package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.TaxIdentifier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "Payer", description = "doc.schema.Payer.description")
public record Payer(

        @Schema(
                description = "doc.schema.Payer.name",
                examples = "Ana Paula Souza",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String name,

        @Schema(
                description = "doc.schema.Payer.taxId",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        TaxIdentifier taxId,

        @Schema(
                description = "doc.schema.Payer.postalCode",
                examples = "01310-100",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "\\d{5}-?\\d{3}")
        String postalCode) {
}
