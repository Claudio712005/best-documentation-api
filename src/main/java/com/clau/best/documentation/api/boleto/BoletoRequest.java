package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleBoletoRequest.class, name = "SIMPLE"),
        @JsonSubTypes.Type(value = HybridBoletoRequest.class, name = "HYBRID"),
        @JsonSubTypes.Type(value = SplitBoletoRequest.class, name = "SPLIT")
})
@Schema(
        name = "BoletoRequest",
        description = "doc.schema.BoletoRequest.description",
        discriminatorProperty = "category",
        subTypes = {
                SimpleBoletoRequest.class,
                HybridBoletoRequest.class,
                SplitBoletoRequest.class
        })
public abstract sealed class BoletoRequest
        permits SimpleBoletoRequest, HybridBoletoRequest, SplitBoletoRequest {

    @Schema(
            description = "doc.schema.BoletoRequest.category",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final BoletoCategory category;

    @Schema(
            description = "doc.schema.BoletoRequest.payer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    private final Payer payer;

    @Schema(
            description = "doc.schema.BoletoRequest.amount",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    private final Money amount;

    @Schema(
            description = "doc.schema.BoletoRequest.dueDate",
            format = "date",
            examples = "2026-12-05",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Future
    private final LocalDate dueDate;

    @Schema(description = "doc.schema.BoletoRequest.reference", examples = "INV-2026-0045")
    private final @Nullable String reference;

    protected BoletoRequest(
            BoletoCategory category,
            Payer payer,
            Money amount,
            LocalDate dueDate,
            @Nullable String reference) {

        this.category = category;
        this.payer = payer;
        this.amount = amount;
        this.dueDate = dueDate;
        this.reference = reference;
    }

    public BoletoCategory getCategory() {
        return category;
    }

    public Payer getPayer() {
        return payer;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public @Nullable String getReference() {
        return reference;
    }
}
