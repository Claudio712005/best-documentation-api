package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Schema(name = "PixInstrument", description = "doc.schema.PixInstrument.description")
public final class PixInstrument extends PaymentInstrument {

    @Schema(
            description = "doc.schema.PixInstrument.keyType",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final PixKeyType keyType;

    @Schema(
            description = "doc.schema.PixInstrument.key",
            examples = {"ana.souza@example.com", "+5511999998888", "12345678901"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 77)
    private final String key;

    @JsonCreator
    public PixInstrument(
            @JsonProperty("label") String label,
            @JsonProperty("holderName") String holderName,
            @JsonProperty("preferred") @Nullable Boolean preferred,
            @JsonProperty("keyType") PixKeyType keyType,
            @JsonProperty("key") String key) {

        super(PaymentInstrumentType.PIX, label, holderName, preferred);
        this.keyType = keyType;
        this.key = key;
    }

    public PixKeyType getKeyType() {
        return keyType;
    }

    public String getKey() {
        return key;
    }

    @Schema(
            name = "PixKeyType",
            description = "doc.schema.PixKeyType.description",
            examples = "EMAIL",
            enumAsRef = true)
    public enum PixKeyType {
        CPF,
        CNPJ,
        EMAIL,
        PHONE,
        RANDOM
    }
}
