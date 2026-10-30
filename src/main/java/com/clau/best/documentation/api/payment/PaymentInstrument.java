package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Base of the payment instrument hierarchy — real Java inheritance, deliberately invisible in the
 * published document.
 *
 * <p>This class exists so the members every instrument shares are declared once. What the document
 * shows instead is a discriminated union of the four concrete types, each carrying these members
 * flattened into it, with no reference back to this class. {@code PaymentInstrumentSchemaCustomizer}
 * performs that collapse; the reasoning for it lives there.
 *
 * <p>Jackson uses {@code EXISTING_PROPERTY} rather than the default {@code PROPERTY} because
 * {@code type} is a declared member of this class, exposed by a getter. That pairing is what makes
 * the discriminator round-trip: Jackson reads it as the type id and does not write a second copy on
 * serialization, while the getter puts the real member in the response. With plain
 * {@code PROPERTY}, Jackson would synthesize the member on the way in and strip it on the way out,
 * leaving a client unable to read back the variant it just sent.
 *
 * <p>{@code visible} stays at its default of {@code false}. Setting it true additionally passes
 * {@code type} down to the subtype's creator, which does not declare that parameter — with
 * {@code spring.jackson.deserialization.fail-on-unknown-properties} enabled, every request then
 * fails as an unparseable body. Nothing is lost by leaving it false, since each subclass already
 * fixes its own type in the {@code super} call.
 *
 * <p>{@code sealed} is load-bearing for the contract, not only for the compiler: the permitted
 * subclasses are the union members, so an instrument added without being listed here fails to
 * compile instead of being silently absent from the document.
 */
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

    /**
     * Declared with JSpecify's {@code @Nullable} rather than {@code @Schema(nullable = true)}.
     *
     * <p>Spring Boot 4 ships JSpecify, and springdoc's Java nullable customizer reads it, so one
     * annotation drives both the static nullness contract and the {@code type: [boolean, "null"]}
     * union in the document — instead of a comment on a field claiming something the compiler
     * cannot check.
     */
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
