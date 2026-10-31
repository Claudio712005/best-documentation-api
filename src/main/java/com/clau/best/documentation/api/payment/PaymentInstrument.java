package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CardInstrument.class, name = "CARD"),
        @JsonSubTypes.Type(value = PixInstrument.class, name = "PIX"),
        @JsonSubTypes.Type(value = BoletoInstrument.class, name = "BOLETO"),
        @JsonSubTypes.Type(value = WalletInstrument.class, name = "WALLET")
})
@Schema(
        name = "PaymentInstrument",
        description = "doc.schema.PaymentInstrument.description",
        discriminatorProperty = "type",
        subTypes = {
                CardInstrument.class,
                PixInstrument.class,
                BoletoInstrument.class,
                WalletInstrument.class
        })
public abstract sealed class PaymentInstrument
        permits CardInstrument, PixInstrument, BoletoInstrument, WalletInstrument {

    @Schema(
            description = "doc.schema.PaymentInstrument.type",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final PaymentInstrumentType type;

    @Schema(
            description = "doc.schema.PaymentInstrument.label",
            examples = "Personal card",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 60)
    private final String label;

    @Schema(
            description = "doc.schema.PaymentInstrument.holderName",
            examples = "ANA P SOUZA",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 120)
    private final String holderName;

    @Schema(description = "doc.schema.PaymentInstrument.preferred", examples = "true")
    private final @Nullable Boolean preferred;

    protected PaymentInstrument(
            PaymentInstrumentType type, String label, String holderName, @Nullable Boolean preferred) {

        this.type = type;
        this.label = label;
        this.holderName = holderName;
        this.preferred = preferred;
    }

    public PaymentInstrumentType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getHolderName() {
        return holderName;
    }

    public @Nullable Boolean getPreferred() {
        return preferred;
    }
}
