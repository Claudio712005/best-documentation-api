package com.clau.best.documentation.api.boleto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.OffsetDateTime;

@Schema(name = "QrCode", description = "doc.schema.QrCode.description")
public record QrCode(

        @Schema(
                description = "doc.schema.QrCode.payload",
                examples = "00020126580014br.gov.bcb.pix0136cobranca@example.com5204000053039865802BR",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String payload,

        @Schema(
                description = "doc.schema.QrCode.imageUri",
                format = "uri",
                examples = "https://api.best-documentation-api.dev/v1/boletos/8c5f2d41/qrcode.png",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI imageUri,

        @Schema(
                description = "doc.schema.QrCode.expiresAt",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime expiresAt) {
}
