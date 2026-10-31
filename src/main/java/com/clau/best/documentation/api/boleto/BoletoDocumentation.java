package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.openapi.DiscriminatedUnion;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class BoletoDocumentation {

    @Bean
    public DiscriminatedUnion boletoRequestUnion() {
        return DiscriminatedUnion.of(
                schemaName(BoletoRequest.class), "category", mapping(BoletoDocumentation::requestVariant));
    }

    @Bean
    public DiscriminatedUnion boletoResponseUnion() {
        return DiscriminatedUnion.of(
                schemaName(BoletoResponse.class), "category", mapping(BoletoDocumentation::responseVariant));
    }

    private static Map<String, String> mapping(Function<BoletoCategory, String> variant) {
        return Arrays.stream(BoletoCategory.values())
                .collect(Collectors.toMap(
                        BoletoCategory::name, variant, (first, second) -> first, LinkedHashMap::new));
    }

    private static String requestVariant(BoletoCategory category) {
        return switch (category) {
            case SIMPLE -> schemaName(SimpleBoletoRequest.class);
            case HYBRID -> schemaName(HybridBoletoRequest.class);
            case SPLIT -> schemaName(SplitBoletoRequest.class);
        };
    }

    private static String responseVariant(BoletoCategory category) {
        return switch (category) {
            case SIMPLE -> schemaName(SimpleBoletoResponse.class);
            case HYBRID -> schemaName(HybridBoletoResponse.class);
            case SPLIT -> schemaName(SplitBoletoResponse.class);
        };
    }

    private static String schemaName(Class<?> type) {
        Schema schema = type.getAnnotation(Schema.class);
        return schema == null || schema.name().isBlank() ? type.getSimpleName() : schema.name();
    }
}
