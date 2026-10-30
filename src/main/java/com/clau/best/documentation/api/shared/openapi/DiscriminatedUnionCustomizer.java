package com.clau.best.documentation.api.shared.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collapses an inheritance hierarchy into a discriminated union of its concrete variants.
 *
 * <p>Out of the box, springdoc publishes a Java hierarchy as the base type plus one
 * {@code allOf: [$ref: Base, {...}]} schema per subclass, and — with its polymorphic converter
 * enabled — an extra {@code <Base>Variant} wrapper holding the {@code oneOf}. Three things are wrong
 * with that for a public contract:
 *
 * <ol>
 *   <li><b>The base type is published.</b> It is an implementation detail: no endpoint accepts or
 *       returns it, yet it appears in the schema list and in generated clients as a type nobody can
 *       construct.
 *   <li><b>The wrapper has no discriminator.</b> springdoc builds the {@code oneOf} but never copies
 *       the base type's discriminator onto it, so a client is told which shapes are possible without
 *       being told which member distinguishes them — and must resort to trial deserialization.
 *   <li><b>A {@code @Schema(implementation = Base.class)} use site points at the base.</b> The
 *       document then claims the endpoint accepts only the shared members, which is simply false.
 * </ol>
 *
 * <p>This customizer flattens each variant so it stands alone, builds the union with a full
 * discriminator mapping, rewrites every reference to the base type or wrapper into that union, and
 * removes both from the components. What remains is a schema list containing only types a client can
 * actually send or receive.
 *
 * <p>References are rewritten by mutating the referring schema in place — clearing its {@code $ref}
 * and setting {@code oneOf} on the same instance — rather than by replacing it inside whichever
 * container holds it. A reference can sit in a property, an array's items, a composed member, a
 * parameter, a request body, a response or a header, and enumerating those containers is where this
 * kind of rewrite usually misses a case.
 */
@Component
public class DiscriminatedUnionCustomizer implements GlobalOpenApiCustomizer {

    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

    private final List<DiscriminatedUnion> unions;

    public DiscriminatedUnionCustomizer(ObjectProvider<DiscriminatedUnion> unions) {
        this.unions = unions.stream().toList();
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        unions.forEach(union -> collapse(openApi, union));
    }

    @SuppressWarnings("rawtypes")
    private void collapse(OpenAPI openApi, DiscriminatedUnion union) {
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        Schema base = schemas.get(union.baseSchema());
        if (base == null) {
            // The family is not part of this document — a grouped doc that excludes those paths.
            return;
        }

        union.mapping().values().forEach(variantName -> {
            Schema variant = schemas.get(variantName);
            if (variant != null) {
                flatten(variant, base, union.baseSchema());
            }
        });

        rewriteReferences(openApi, union);

        schemas.remove(union.baseSchema());
        if (union.variantSchema() != null) {
            schemas.remove(union.variantSchema());
        }
    }

    /**
     * Rewrites a variant from {@code allOf: [$ref: Base, {...}]} into a standalone object schema.
     *
     * <p>Members declared by the variant win over the inherited ones, which is what preserves an
     * override — a subclass narrowing an inherited member's constraints keeps its own version.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void flatten(Schema variant, Schema base, String baseSchemaName) {
        List<Schema> composed = variant.getAllOf();
        if (composed == null || composed.isEmpty()) {
            return;
        }

        Map<String, Schema> properties = new LinkedHashMap<>();
        if (base.getProperties() != null) {
            properties.putAll(base.getProperties());
        }
        Set<String> required = new TreeSet<>();
        if (base.getRequired() != null) {
            required.addAll(base.getRequired());
        }
        if (variant.getRequired() != null) {
            required.addAll(variant.getRequired());
        }

        List<Schema> unresolved = new ArrayList<>();
        for (Schema member : composed) {
            if (isReferenceTo(member, baseSchemaName)) {
                continue;
            }
            if (member.get$ref() != null) {
                // A composed member referencing something other than the base type: not ours to
                // flatten, so it is kept and the variant stays composed.
                unresolved.add(member);
                continue;
            }
            if (member.getProperties() != null) {
                properties.putAll(member.getProperties());
            }
            if (member.getRequired() != null) {
                required.addAll(member.getRequired());
            }
        }

        variant.setAllOf(unresolved.isEmpty() ? null : unresolved);
        // OpenAPI 3.1 serializes the type from `types`; the legacy single-valued `type` field is
        // ignored by the 3.1 mixin, so setting only that one emits a schema with no declared type.
        variant.setTypes(new LinkedHashSet<>(List.of("object")));
        variant.setProperties(properties);
        variant.setRequired(required.isEmpty() ? null : new ArrayList<>(required));
    }

    /** Turns every reference to the base type or its wrapper into the discriminated union itself. */
    @SuppressWarnings("rawtypes")
    private void rewriteReferences(OpenAPI openApi, DiscriminatedUnion union) {
        DocumentGraph.forEachSchema(openApi, schema -> {
            if (isReferenceTo(schema, union.baseSchema())
                    || isReferenceTo(schema, union.variantSchema())) {

                schema.set$ref(null);
                schema.setTypes(null);
                schema.setOneOf(variantReferences(union));
                schema.setDiscriminator(discriminator(union));
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private List<Schema> variantReferences(DiscriminatedUnion union) {
        return union.mapping().values().stream()
                .distinct()
                .sorted()
                .<Schema>map(name -> new Schema<>().$ref(SCHEMA_REF_PREFIX + name))
                .toList();
    }

    private Discriminator discriminator(DiscriminatedUnion union) {
        Map<String, String> mapping = new LinkedHashMap<>();
        union.mapping().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> mapping.put(entry.getKey(), SCHEMA_REF_PREFIX + entry.getValue()));

        return new Discriminator().propertyName(union.discriminatorProperty()).mapping(mapping);
    }

    @SuppressWarnings("rawtypes")
    private static boolean isReferenceTo(Schema schema, String schemaName) {
        return schemaName != null
                && schema.get$ref() != null
                && schema.get$ref().equals(SCHEMA_REF_PREFIX + schemaName);
    }
}
