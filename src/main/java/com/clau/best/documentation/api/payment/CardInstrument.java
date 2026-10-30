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

/**
 * A payment card.
 *
 * <p>The widest member of the union, and the one that shows why the discriminator matters: nothing
 * else in this family carries an expiry or a security code, so a client cannot validate the payload
 * without first knowing which variant it is reading.
 */
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

    /**
     * Write-only, declared with Jackson's {@code access} rather than {@code @Schema(writeOnly)}.
     *
     * <p>The distinction matters more than it looks. {@code @Schema(writeOnly = true)} only writes
     * {@code writeOnly: true} into the document; Jackson keeps serializing the field, so the
     * contract promises the value never comes back while the running service returns it on every
     * response. {@code @JsonProperty(access = WRITE_ONLY)} drops it from serialization and springdoc
     * reads the same annotation to emit {@code writeOnly: true} — one annotation, with the document
     * and the behaviour unable to disagree.
     */
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
