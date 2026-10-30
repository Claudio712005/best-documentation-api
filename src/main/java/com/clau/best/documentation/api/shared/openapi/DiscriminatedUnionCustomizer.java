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

        variant.setTypes(new LinkedHashSet<>(List.of("object")));
        variant.setProperties(properties);
        variant.setRequired(required.isEmpty() ? null : new ArrayList<>(required));
    }

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
