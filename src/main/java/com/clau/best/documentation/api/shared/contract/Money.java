package com.clau.best.documentation.api.shared.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(name = "Money", description = "doc.schema.Money.description")
public record Money(

        @Schema(
                description = "doc.schema.Money.amount",
                examples = "149.90",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        @Digits(integer = 13, fraction = 2, message = "{validation.money.amount}")
        BigDecimal amount,

        @Schema(
                description = "doc.schema.Money.currency",
                examples = "BRL",
                minLength = 3,
                maxLength = 3,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Pattern(regexp = "[A-Z]{3}", message = "{validation.money.currency}")
        String currency) {
}
