package com.clau.best.documentation.api.payment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A stored payment instrument.
 *
 * <p>Composition rather than inheritance: the server-assigned members sit in this wrapper and the
 * client-supplied ones stay in the nested union. The alternative — adding {@code id} and
 * {@code registeredAt} to the hierarchy — would force every request variant to document two members
 * a client must never send.
 *
 * <p>It also puts the union at a second use site, which is the honest cost of inlining it: the
 * {@code oneOf} appears both here and in the request body.
 */
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
