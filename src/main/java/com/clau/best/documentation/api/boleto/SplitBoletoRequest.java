package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "SplitBoletoRequest", description = "doc.schema.SplitBoletoRequest.description")
public final class SplitBoletoRequest extends BoletoRequest {

    @ArraySchema(
            arraySchema = @Schema(
                    description = "doc.schema.SplitBoletoRequest.recipients",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(implementation = SplitRecipient.class),
            minItems = 2,
            maxItems = 10,
            uniqueItems = false)
    @NotEmpty
    @Size(min = 2, max = 10)
    @Valid
    private final List<SplitRecipient> recipients;

    @Schema(
            description = "doc.schema.SplitBoletoRequest.settlementMode",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final SettlementMode settlementMode;

    @Schema(description = "doc.schema.SplitBoletoRequest.statementDescriptor", examples = "MARKETPLACE")
    private final @Nullable String statementDescriptor;

    @JsonCreator
    public SplitBoletoRequest(
            @JsonProperty("payer") Payer payer,
            @JsonProperty("amount") Money amount,
            @JsonProperty("dueDate") LocalDate dueDate,
            @JsonProperty("reference") @Nullable String reference,
            @JsonProperty("recipients") List<SplitRecipient> recipients,
            @JsonProperty("settlementMode") SettlementMode settlementMode,
            @JsonProperty("statementDescriptor") @Nullable String statementDescriptor) {

        super(BoletoCategory.SPLIT, payer, amount, dueDate, reference);
        this.recipients = recipients == null ? List.of() : List.copyOf(recipients);
        this.settlementMode = settlementMode;
        this.statementDescriptor = statementDescriptor;
    }

    public List<SplitRecipient> getRecipients() {
        return recipients;
    }

    public SettlementMode getSettlementMode() {
        return settlementMode;
    }

    public @Nullable String getStatementDescriptor() {
        return statementDescriptor;
    }

    @Schema(
            name = "SettlementMode",
            description = "doc.schema.SettlementMode.description",
            examples = "ON_PAYMENT",
            enumAsRef = true)
    public enum SettlementMode {
        ON_PAYMENT,
        ON_SCHEDULE
    }
}
