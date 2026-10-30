package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

@Schema(name = "CardInstrument", description = "doc.schema.CardInstrument.description")
public final class CardInstrument extends PaymentInstrument {

    @Schema(
            description = "doc.schema.CardInstrument.number",
            examples = "4111111111111111",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Pattern(regexp = "\\d{13,19}")
    private final String number;

    @Schema(
            description = "doc.schema.CardInstrument.expiryMonth",
            examples = "9",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    @Max(12)
    private final Integer expiryMonth;

    @Schema(
            description = "doc.schema.CardInstrument.expiryYear",
            examples = "2031",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(2026)
    @Max(2099)
    private final Integer expiryYear;

    @Schema(
            description = "doc.schema.CardInstrument.securityCode",
            examples = "123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Pattern(regexp = "\\d{3,4}")
    private final String securityCode;

    @JsonCreator
    public CardInstrument(
            @JsonProperty("label") String label,
            @JsonProperty("holderName") String holderName,
            @JsonProperty("preferred") @Nullable Boolean preferred,
            @JsonProperty("number") String number,
            @JsonProperty("expiryMonth") Integer expiryMonth,
            @JsonProperty("expiryYear") Integer expiryYear,
            @JsonProperty("securityCode") String securityCode) {

        super(PaymentInstrumentType.CARD, label, holderName, preferred);
        this.number = number;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.securityCode = securityCode;
    }

    public String getNumber() {
        return number;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public String getSecurityCode() {
        return securityCode;
    }
}
