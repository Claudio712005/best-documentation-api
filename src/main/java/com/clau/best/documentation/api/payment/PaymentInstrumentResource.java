package com.clau.best.documentation.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "PaymentInstrumentResource", description = "doc.schema.PaymentInstrumentResource.description")
public record PaymentInstrumentResource(

        @Schema(
                description = "doc.schema.PaymentInstrumentResource.id",
                format = "uuid",
                examples = "3f1a7c9e-5b2d-4e81-9a6f-0c7d8e2b4a15",
                requiredMode = Schema.RequiredMode.REQUIRED,
                accessMode = Schema.AccessMode.READ_ONLY)
        UUID id,

        @Schema(
                description = "doc.schema.PaymentInstrumentResource.registeredAt",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED,
                accessMode = Schema.AccessMode.READ_ONLY)
        OffsetDateTime registeredAt,

        @Schema(
                description = "doc.schema.PaymentInstrumentResource.instrument",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PaymentInstrument instrument) {
}
