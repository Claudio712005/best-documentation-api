package com.clau.best.documentation.api.shared.openapi;

import java.util.Map;

public record DiscriminatedUnion(
        String baseSchema,
        String variantSchema,
        String discriminatorProperty,
        Map<String, String> mapping) {

    public DiscriminatedUnion {
        mapping = Map.copyOf(mapping);
    }

    public static DiscriminatedUnion of(
            String baseSchema, String discriminatorProperty, Map<String, String> mapping) {

        return new DiscriminatedUnion(
                baseSchema, baseSchema + "Variant", discriminatorProperty, mapping);
    }
}
