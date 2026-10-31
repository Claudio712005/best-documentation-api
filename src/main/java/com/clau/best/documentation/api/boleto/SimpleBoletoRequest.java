package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Schema(name = "SimpleBoletoRequest", description = "doc.schema.SimpleBoletoRequest.description")
public final class SimpleBoletoRequest extends BoletoRequest {

    @JsonCreator
    public SimpleBoletoRequest(
            @JsonProperty("payer") Payer payer,
            @JsonProperty("amount") Money amount,
            @JsonProperty("dueDate") LocalDate dueDate,
            @JsonProperty("reference") @Nullable String reference) {

        super(BoletoCategory.SIMPLE, payer, amount, dueDate, reference);
    }
}
