package com.clau.best.documentation.api.system;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LanguageDescriptor",
        description = "doc.schema.LanguageDescriptor.description")
public record LanguageDescriptor(

        @Schema(description = "doc.schema.LanguageDescriptor.tag", examples = {"pt-BR", "en-US"})
        String tag,

        @Schema(description = "doc.schema.LanguageDescriptor.displayName", examples = "espanhol (Espanha)")
        String displayName,

        @Schema(description = "doc.schema.LanguageDescriptor.negotiated", examples = "true")
        boolean negotiated,

        @Schema(description = "doc.schema.LanguageDescriptor.fallback", examples = "false")
        boolean fallback) {
}
