package com.clau.best.documentation.api.system;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One language this API can serve.
 *
 * <p>Published as an endpoint rather than left to the prose of the document, so a client can
 * discover the supported set at runtime instead of hard-coding a copy that goes stale.
 */
@Schema(
        name = "LanguageDescriptor",
        description = "doc.schema.LanguageDescriptor.description")
public record LanguageDescriptor(

        @Schema(description = "doc.schema.LanguageDescriptor.tag", examples = {"pt-BR", "en-US"})
        String tag,

        /**
         * Rendered in the language the caller asked for, not in the language being described — so a
         * Portuguese-speaking client sees "espanhol (Espanha)" rather than "español (España)".
         */
        @Schema(description = "doc.schema.LanguageDescriptor.displayName", examples = "espanhol (Espanha)")
        String displayName,

        @Schema(description = "doc.schema.LanguageDescriptor.negotiated", examples = "true")
        boolean negotiated,

        @Schema(description = "doc.schema.LanguageDescriptor.fallback", examples = "false")
        boolean fallback) {
}
