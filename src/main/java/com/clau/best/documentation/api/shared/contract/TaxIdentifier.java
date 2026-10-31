package com.clau.best.documentation.api.shared.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "TaxIdentifier", description = "doc.schema.TaxIdentifier.description")
public record TaxIdentifier(

        @Schema(
                description = "doc.schema.TaxIdentifier.value",
                examples = {"12345678901", "12345678000195"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "{validation.boleto.taxId}")
        String value,

        @Schema(
                description = "doc.schema.TaxIdentifier.kind",
                requiredMode = Schema.RequiredMode.REQUIRED,
                accessMode = Schema.AccessMode.READ_ONLY)
        Kind kind) {

    private static final int COMPANY_LENGTH = 14;

    public TaxIdentifier {
        kind = value != null && value.length() == COMPANY_LENGTH ? Kind.COMPANY : Kind.INDIVIDUAL;
    }

    @Schema(name = "TaxIdentifierKind", description = "doc.schema.TaxIdentifierKind.description", enumAsRef = true)
    public enum Kind {
        INDIVIDUAL,
        COMPANY
    }
}
