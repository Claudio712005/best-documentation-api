package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "SplitBoletoResponse", description = "doc.schema.SplitBoletoResponse.description")
public final class SplitBoletoResponse extends BoletoResponse {

    @ArraySchema(
            arraySchema = @Schema(
                    description = "doc.schema.SplitBoletoResponse.splits",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(implementation = SplitAllocation.class),
            minItems = 2)
    private final List<SplitAllocation> splits;

    @Schema(
            description = "doc.schema.SplitBoletoResponse.settlementMode",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final SplitBoletoRequest.SettlementMode settlementMode;

    public SplitBoletoResponse(
            UUID id,
            BoletoStatus status,
            String barcode,
            String digitableLine,
            Money amount,
            LocalDate dueDate,
            OffsetDateTime issuedAt,
            List<SplitAllocation> splits,
            SplitBoletoRequest.SettlementMode settlementMode) {

        super(id, BoletoCategory.SPLIT, status, barcode, digitableLine, amount, dueDate, issuedAt);
        this.splits = List.copyOf(splits);
        this.settlementMode = settlementMode;
    }

    public List<SplitAllocation> getSplits() {
        return splits;
    }

    public SplitBoletoRequest.SettlementMode getSettlementMode() {
        return settlementMode;
    }
}
