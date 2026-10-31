package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleBoletoResponse.class, name = "SIMPLE"),
        @JsonSubTypes.Type(value = HybridBoletoResponse.class, name = "HYBRID"),
        @JsonSubTypes.Type(value = SplitBoletoResponse.class, name = "SPLIT")
})
@Schema(
        name = "BoletoResponse",
        description = "doc.schema.BoletoResponse.description",
        discriminatorProperty = "category",
        subTypes = {
                SimpleBoletoResponse.class,
                HybridBoletoResponse.class,
                SplitBoletoResponse.class
        })
public abstract sealed class BoletoResponse
        permits SimpleBoletoResponse, HybridBoletoResponse, SplitBoletoResponse {

    @Schema(
            description = "doc.schema.BoletoResponse.id",
            format = "uuid",
            examples = "8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID id;

    @Schema(
            description = "doc.schema.BoletoResponse.category",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final BoletoCategory category;

    @Schema(
            description = "doc.schema.BoletoResponse.status",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final BoletoStatus status;

    @Schema(
            description = "doc.schema.BoletoResponse.barcode",
            examples = "34191790010104351004791020150008292260000014990",
            minLength = 44,
            maxLength = 48,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final String barcode;

    @Schema(
            description = "doc.schema.BoletoResponse.digitableLine",
            examples = "34191.79001 01043.510047 91020.150008 2 92260000014990",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final String digitableLine;

    @Schema(
            description = "doc.schema.BoletoResponse.amount",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final Money amount;

    @Schema(
            description = "doc.schema.BoletoResponse.dueDate",
            format = "date",
            examples = "2026-12-05",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final LocalDate dueDate;

    @Schema(
            description = "doc.schema.BoletoResponse.issuedAt",
            format = "date-time",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final OffsetDateTime issuedAt;

    protected BoletoResponse(
            UUID id,
            BoletoCategory category,
            BoletoStatus status,
            String barcode,
            String digitableLine,
            Money amount,
            LocalDate dueDate,
            OffsetDateTime issuedAt) {

        this.id = id;
        this.category = category;
        this.status = status;
        this.barcode = barcode;
        this.digitableLine = digitableLine;
        this.amount = amount;
        this.dueDate = dueDate;
        this.issuedAt = issuedAt;
    }

    public UUID getId() {
        return id;
    }

    public BoletoCategory getCategory() {
        return category;
    }

    public BoletoStatus getStatus() {
        return status;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDigitableLine() {
        return digitableLine;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    @Schema(
            name = "BoletoStatus",
            description = "doc.schema.BoletoStatus.description",
            examples = "ISSUED",
            enumAsRef = true)
    public enum BoletoStatus {
        ISSUED,
        SETTLED,
        CANCELLED,
        EXPIRED
    }
}
