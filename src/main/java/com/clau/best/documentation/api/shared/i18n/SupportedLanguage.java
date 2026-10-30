package com.clau.best.documentation.api.shared.i18n;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Schema(
        name = "SupportedLanguage",
        description = "doc.schema.SupportedLanguage.description",
        examples = "pt-BR",
        enumAsRef = true)
public enum SupportedLanguage {

    EN_US(Locale.of("en", "US")),

    PT_BR(Locale.of("pt", "BR")),

    ES_ES(Locale.of("es", "ES"));

    public static final SupportedLanguage DEFAULT = EN_US;

    private final Locale locale;

    SupportedLanguage(Locale locale) {
        this.locale = locale;
    }

    public Locale locale() {
        return locale;
    }

    public String tag() {
        return locale.toLanguageTag();
    }

    public static List<Locale> locales() {
        return Arrays.stream(values()).map(SupportedLanguage::locale).toList();
    }

    public static Optional<SupportedLanguage> fromTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return Optional.empty();
        }
        String normalized = Locale.forLanguageTag(tag.trim()).toLanguageTag();
        return Arrays.stream(values())
                .filter(language -> language.tag().equalsIgnoreCase(normalized))
                .findFirst();
    }

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
