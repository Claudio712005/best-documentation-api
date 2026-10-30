package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.shared.openapi.DiscriminatedUnion;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class PaymentInstrumentDocumentation {

    @Bean
    public DiscriminatedUnion paymentInstrumentUnion() {
        Map<String, String> mapping = Arrays.stream(PaymentInstrumentType.values())
                .collect(Collectors.toMap(
                        PaymentInstrumentType::name,
                        PaymentInstrumentDocumentation::variantSchemaName,
                        (first, second) -> first,
                        LinkedHashMap::new));

        return DiscriminatedUnion.of(schemaName(PaymentInstrument.class), "type", mapping);
    }

    private static String variantSchemaName(PaymentInstrumentType type) {
        return switch (type) {
            case CARD -> schemaName(CardInstrument.class);
            case PIX -> schemaName(PixInstrument.class);
            case BOLETO -> schemaName(BoletoInstrument.class);
            case WALLET -> schemaName(WalletInstrument.class);
        };
    }

    private static String schemaName(Class<?> type) {
        Schema schema = type.getAnnotation(Schema.class);
        return schema == null || schema.name().isBlank()
                ? type.getSimpleName()
                : schema.name();
    }
}
