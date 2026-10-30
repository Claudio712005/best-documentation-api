package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

/** A bank slip payer registration. */
@Schema(name = "BoletoInstrument", description = "doc.schema.BoletoInstrument.description")
public final class BoletoInstrument extends PaymentInstrument {

    @Schema(
            description = "doc.schema.BoletoInstrument.taxId",
            examples = {"12345678901", "12345678000195"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "{validation.boleto.taxId}")
    private final String taxId;

    @Schema(
            description = "doc.schema.BoletoInstrument.postalCode",
            examples = "01310-100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Pattern(regexp = "\\d{5}-?\\d{3}")
    private final String postalCode;

    @JsonCreator
    public BoletoInstrument(
            @JsonProperty("label") String label,
            @JsonProperty("holderName") String holderName,
            @JsonProperty("preferred") @Nullable Boolean preferred,
            @JsonProperty("taxId") String taxId,
            @JsonProperty("postalCode") String postalCode) {

        super(PaymentInstrumentType.BOLETO, label, holderName, preferred);
        this.taxId = taxId;
        this.postalCode = postalCode;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
