package com.clau.best.documentation.api.shared.openapi;

import java.util.Map;

/**
 * Declares one polymorphic family that should be published as a discriminated union of its concrete
 * variants, with the base type absent from the document.
 *
 * <p>Declared explicitly, as a bean, rather than discovered by scanning for abstract classes. A
 * rewrite this aggressive should be opt-in and reviewable: reading this declaration tells you
 * exactly which families collapse and what the discriminator values are, and adding a variant to the
 * code without listing it here leaves the document unchanged rather than silently restructured.
 *
 * @param baseSchema schema name of the Java base type, e.g. {@code PaymentInstrument}
 * @param variantSchema name of the wrapper springdoc's polymorphic converter generates for the
 *     family, conventionally {@code <baseSchema>Variant}; may be {@code null} when the converter is
 *     disabled
 * @param discriminatorProperty member carrying the variant identifier, e.g. {@code type}
 * @param mapping discriminator value to variant schema name, e.g. {@code CARD -> CardInstrument}
 */
public record DiscriminatedUnion(
        String baseSchema,
        String variantSchema,
        String discriminatorProperty,
        Map<String, String> mapping) {

    public DiscriminatedUnion {
        mapping = Map.copyOf(mapping);
    }

    /** Convention used by springdoc's {@code one-of-ref-suffix}, whose default is {@code Variant}. */
    public static DiscriminatedUnion of(
            String baseSchema, String discriminatorProperty, Map<String, String> mapping) {

        return new DiscriminatedUnion(
                baseSchema, baseSchema + "Variant", discriminatorProperty, mapping);
    }
}
