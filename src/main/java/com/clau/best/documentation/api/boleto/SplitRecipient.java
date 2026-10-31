package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.TaxIdentifier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "SplitRecipient", description = "doc.schema.SplitRecipient.description")
public record SplitRecipient(

        @Schema(
                description = "doc.schema.SplitRecipient.name",
                examples = "Marketplace Seller Ltda",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String name,

        @Schema(
                description = "doc.schema.SplitRecipient.taxId",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        TaxIdentifier taxId,

        @Schema(
                description = "doc.schema.SplitRecipient.percentage",
                examples = "70.00",
                minimum = "0.01",
                maximum = "100.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal percentage) {
}
