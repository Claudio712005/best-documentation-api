package com.clau.best.documentation.api.shared.i18n;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Languages this API is able to negotiate through the {@code Accept-Language} header.
 *
 * <p>Kept as a closed enum on purpose: the set of supported languages is part of the public
 * contract, so it must appear in the OpenAPI document instead of being an open-ended string.
 */
@Schema(
        name = "SupportedLanguage",
        description = "Language tag accepted by the `Accept-Language` header, in BCP 47 form.",
        example = "pt-BR")
public enum SupportedLanguage {

    /** English (United States) — the fallback language of the API. */
    EN_US(Locale.of("en", "US")),

    /** Brazilian Portuguese. */
    PT_BR(Locale.of("pt", "BR")),

    /** European Spanish. */
    ES_ES(Locale.of("es", "ES"));

    /** Language used whenever the client sends no acceptable {@code Accept-Language}. */
    public static final SupportedLanguage DEFAULT = EN_US;

    private final Locale locale;

    SupportedLanguage(Locale locale) {
        this.locale = locale;
    }

    public Locale locale() {
        return locale;
    }

    /** BCP 47 tag, e.g. {@code pt-BR}. This is the wire format used in payloads and headers. */
    public String tag() {
        return locale.toLanguageTag();
    }

    public static List<Locale> locales() {
        return Arrays.stream(values()).map(SupportedLanguage::locale).toList();
    }

    /** Resolves a BCP 47 tag case-insensitively; empty when the language is not supported. */
    public static Optional<SupportedLanguage> fromTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return Optional.empty();
        }
        String normalized = Locale.forLanguageTag(tag.trim()).toLanguageTag();
        return Arrays.stream(values())
                .filter(language -> language.tag().equalsIgnoreCase(normalized))
                .findFirst();
    }

    /** Maps any locale onto the closest supported language, falling back to {@link #DEFAULT}. */
    public static SupportedLanguage closestTo(Locale locale) {
        if (locale == null) {
            return DEFAULT;
        }
        return fromTag(locale.toLanguageTag())
                .or(() -> Arrays.stream(values())
                        .filter(language -> language.locale().getLanguage().equals(locale.getLanguage()))
                        .findFirst())
                .orElse(DEFAULT);
    }
}
