package com.clau.best.documentation.api.config;

import com.clau.best.documentation.api.shared.openapi.DocumentGraph;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Translates the OpenAPI document into the language the client negotiated.
 *
 * <p>springdoc already resolves message keys for operations, tags, parameters and responses, since
 * those paths receive the request locale. Two things it does not cover, and this customizer does:
 *
 * <ol>
 *   <li><b>Schemas.</b> The built-in resolver enabled by
 *       {@code springdoc.api-docs.resolve-schema-properties} calls
 *       {@code resolveProperties(schema, Locale.getDefault())} — the JVM default locale, not the
 *       request locale — so it would freeze every schema description in the server's boot language.
 *       That flag is switched off in configuration and this class replaces it, walking inline
 *       schemas as well as the named ones.
 *   <li><b>The programmatic {@code OpenAPI} bean.</b> Key resolution applies to annotation values,
 *       so {@code info} and tag prose built in {@link OpenApiConfig} would never be translated.
 * </ol>
 *
 * <p>Implemented as an {@link OpenApiLocaleCustomizer} rather than a plain {@code OpenApiCustomizer}
 * because only this interface is handed the locale springdoc settled on after applying
 * {@code springdoc.allowed-locales}. Reading the current thread's locale instead would see whatever
 * the servlet request carried, which is not necessarily the same value.
 *
 * <p>Mutating the document in place is safe: {@code OpenAPIService#build(Locale)} deep-copies the
 * {@code OpenAPI} bean through JSON for every build, and each locale is cached separately, so one
 * language's resolved text can never become another language's lookup key.
 */
@Component
public class DocumentLocalizationCustomizer implements OpenApiLocaleCustomizer {

    private static final String INFO_SUMMARY_KEY = "doc.info.summary";
    private static final String INFO_DESCRIPTION_KEY = "doc.info.description";
    private static final String TAG_DESCRIPTION_KEY = "doc.tag.%s.description";

    private final MessageSource messageSource;

    public DocumentLocalizationCustomizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void customise(OpenAPI openApi, Locale locale) {
        localizeInfo(openApi, locale);
        localizeTags(openApi.getTags(), locale);

        DocumentGraph.forEachSchema(openApi, schema -> {
            schema.setTitle(resolve(schema.getTitle(), locale));
            schema.setDescription(resolve(schema.getDescription(), locale));
        });

        // springdoc does not resolve keys inside @ExampleObject, so an example's summary and
        // description reach the document verbatim. Left alone, these are the only keys that leak.
        DocumentGraph.forEachExample(openApi, example -> {
            example.setSummary(resolve(example.getSummary(), locale));
            example.setDescription(resolve(example.getDescription(), locale));
        });
    }

    private void localizeInfo(OpenAPI openApi, Locale locale) {
        if (openApi.getInfo() == null) {
            return;
        }
        openApi.getInfo()
                .summary(resolve(INFO_SUMMARY_KEY, locale))
                .description(resolve(INFO_DESCRIPTION_KEY, locale));
    }

    /**
     * Fills in tag descriptions from the tag name.
     *
     * <p>{@link OpenApiConfig} declares names only, so the key is derived rather than repeated — a
     * tag cannot end up pointing at another tag's description.
     */
    private void localizeTags(List<Tag> tags, Locale locale) {
        if (tags == null) {
            return;
        }
        tags.forEach(tag -> tag.setDescription(
                resolve(TAG_DESCRIPTION_KEY.formatted(tag.getName()), locale)));
    }

    /**
     * Looks a value up as a message key, returning it untouched when no translation exists.
     *
     * <p>Falling back to the key rather than failing is what lets a schema carry plain prose when it
     * needs no translation, and keeps a forgotten bundle entry visible in the document instead of
     * breaking the endpoint that serves it.
     */
    private String resolve(String key, Locale locale) {
        if (key == null || key.isBlank()) {
            return key;
        }
        return messageSource.getMessage(key, null, key, locale);
    }
}
