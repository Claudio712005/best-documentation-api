package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import com.clau.best.documentation.api.shared.contract.TaxIdentifier;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "SplitAllocation", description = "doc.schema.SplitAllocation.description")
public record SplitAllocation(

        @Schema(
                description = "doc.schema.SplitAllocation.name",
                examples = "Marketplace Seller Ltda",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(
                description = "doc.schema.SplitAllocation.taxId",
                requiredMode = Schema.RequiredMode.REQUIRED)
        TaxIdentifier taxId,

        @Schema(
                description = "doc.schema.SplitAllocation.percentage",
                examples = "70.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal percentage,

        @Schema(
                description = "doc.schema.SplitAllocation.amount",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Money amount) {
}
