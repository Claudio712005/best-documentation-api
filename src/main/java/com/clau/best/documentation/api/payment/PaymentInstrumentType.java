package com.clau.best.documentation.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Discriminator values of the payment instrument union.
 *
 * <p>These are the values a client writes into the {@code type} member, and the keys of the
 * {@code discriminator.mapping} in the published document. Both come from this one enum, so a new
 * instrument cannot be added to the code without appearing in the contract.
 */
@Schema(
        name = "PaymentInstrumentType",
        description = "doc.schema.PaymentInstrumentType.description",
        examples = "CARD",
        enumAsRef = true)
public enum PaymentInstrumentType {

    CARD,
    PIX,
    BOLETO,
    WALLET
}
