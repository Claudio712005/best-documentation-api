package com.clau.best.documentation.api.config;

import com.clau.best.documentation.api.shared.openapi.DocumentGraph;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

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

    private void localizeTags(List<Tag> tags, Locale locale) {
        if (tags == null) {
            return;
        }
        tags.forEach(tag -> tag.setDescription(
                resolve(TAG_DESCRIPTION_KEY.formatted(tag.getName()), locale)));
    }

    private String resolve(String key, Locale locale) {
        if (key == null || key.isBlank()) {
            return key;
        }
        return messageSource.getMessage(key, null, key, locale);
    }
}
