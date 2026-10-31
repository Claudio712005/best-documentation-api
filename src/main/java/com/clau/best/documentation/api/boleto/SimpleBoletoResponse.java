package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "SimpleBoletoResponse", description = "doc.schema.SimpleBoletoResponse.description")
public final class SimpleBoletoResponse extends BoletoResponse {

    public SimpleBoletoResponse(
            UUID id,
            BoletoStatus status,
            String barcode,
            String digitableLine,
            Money amount,
            LocalDate dueDate,
            OffsetDateTime issuedAt) {

        super(id, BoletoCategory.SIMPLE, status, barcode, digitableLine, amount, dueDate, issuedAt);
    }
}
