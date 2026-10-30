package com.clau.best.documentation.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;

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
