package com.clau.best.documentation.api.boleto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "BoletoCategory",
        description = "doc.schema.BoletoCategory.description",
        examples = "HYBRID",
        enumAsRef = true)
public enum BoletoCategory {

    SIMPLE,
    HYBRID,
    SPLIT
}
