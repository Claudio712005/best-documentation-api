package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Schema(name = "HybridBoletoRequest", description = "doc.schema.HybridBoletoRequest.description")
public final class HybridBoletoRequest extends BoletoRequest {

    @Schema(
            description = "doc.schema.HybridBoletoRequest.pixKey",
            examples = "cobranca@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 77)
    private final String pixKey;

    @Schema(
            description = "doc.schema.HybridBoletoRequest.qrCodeValidityMinutes",
            examples = "60",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(5)
    @Max(1440)
    private final Integer qrCodeValidityMinutes;

    @Schema(description = "doc.schema.HybridBoletoRequest.allowPartialPayment", examples = "false")
    private final @Nullable Boolean allowPartialPayment;

    @JsonCreator
    public HybridBoletoRequest(
            @JsonProperty("payer") Payer payer,
            @JsonProperty("amount") Money amount,
            @JsonProperty("dueDate") LocalDate dueDate,
            @JsonProperty("reference") @Nullable String reference,
            @JsonProperty("pixKey") String pixKey,
            @JsonProperty("qrCodeValidityMinutes") Integer qrCodeValidityMinutes,
            @JsonProperty("allowPartialPayment") @Nullable Boolean allowPartialPayment) {

        super(BoletoCategory.HYBRID, payer, amount, dueDate, reference);
        this.pixKey = pixKey;
        this.qrCodeValidityMinutes = qrCodeValidityMinutes;
        this.allowPartialPayment = allowPartialPayment;
    }

    public String getPixKey() {
        return pixKey;
    }

    public Integer getQrCodeValidityMinutes() {
        return qrCodeValidityMinutes;
    }

    public @Nullable Boolean getAllowPartialPayment() {
        return allowPartialPayment;
    }
}
