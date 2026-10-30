package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.shared.openapi.DiscriminatedUnion;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Declares how the payment instrument hierarchy is published.
 *
 * <p>The mapping is derived rather than written out. An exhaustive {@code switch} over
 * {@link PaymentInstrumentType} means a new instrument type breaks this compilation until it is
 * mapped, and reading each variant's schema name from its own {@code @Schema} annotation means
 * renaming a schema cannot leave a dangling discriminator mapping behind. Both failures are silent
 * if the mapping is a literal map, and both produce a document that lies rather than one that fails.
 */
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

    /**
     * Binds each discriminator value to the class that implements it.
     *
     * <p>Exhaustive by construction: adding a constant to {@link PaymentInstrumentType} without a
     * branch here is a compilation error.
     */
    private static String variantSchemaName(PaymentInstrumentType type) {
        return switch (type) {
            case CARD -> schemaName(CardInstrument.class);
            case PIX -> schemaName(PixInstrument.class);
            case BOLETO -> schemaName(BoletoInstrument.class);
            case WALLET -> schemaName(WalletInstrument.class);
        };
    }

    /** Reads the published schema name off the type itself, defaulting to the simple class name. */
    private static String schemaName(Class<?> type) {
        Schema schema = type.getAnnotation(Schema.class);
        return schema == null || schema.name().isBlank()
                ? type.getSimpleName()
                : schema.name();
    }
}
