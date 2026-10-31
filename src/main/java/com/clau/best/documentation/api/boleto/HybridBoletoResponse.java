package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "HybridBoletoResponse", description = "doc.schema.HybridBoletoResponse.description")
public final class HybridBoletoResponse extends BoletoResponse {

    @Schema(
            description = "doc.schema.HybridBoletoResponse.qrCode",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final QrCode qrCode;

    @Schema(
            description = "doc.schema.HybridBoletoResponse.partialPaymentAllowed",
            examples = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean partialPaymentAllowed;

    public HybridBoletoResponse(
            UUID id,
            BoletoStatus status,
            String barcode,
            String digitableLine,
            Money amount,
            LocalDate dueDate,
            OffsetDateTime issuedAt,
            QrCode qrCode,
            boolean partialPaymentAllowed) {

        super(id, BoletoCategory.HYBRID, status, barcode, digitableLine, amount, dueDate, issuedAt);
        this.qrCode = qrCode;
        this.partialPaymentAllowed = partialPaymentAllowed;
    }

    public QrCode getQrCode() {
        return qrCode;
    }

    public boolean isPartialPaymentAllowed() {
        return partialPaymentAllowed;
    }
}
