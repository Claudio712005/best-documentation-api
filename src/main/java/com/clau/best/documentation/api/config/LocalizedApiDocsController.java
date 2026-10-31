package com.clau.best.documentation.api.config;

import com.clau.best.documentation.api.shared.error.ApiException;
import com.clau.best.documentation.api.shared.i18n.SupportedLanguage;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequestMapping(LocalizedApiDocsController.LANGUAGE_PATH)
public class LocalizedApiDocsController {

    static final String LANGUAGE_PATH = Constants.API_DOCS_URL + "/language";

    private final OpenApiWebMvcResource openApiResource;
    private final String apiDocsUrl;

    public LocalizedApiDocsController(
            OpenApiWebMvcResource openApiResource,
            @Value(Constants.API_DOCS_URL) String apiDocsUrl) {

        this.openApiResource = openApiResource;
        this.apiDocsUrl = apiDocsUrl;
    }

    @GetMapping(path = "/{language}", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] json(HttpServletRequest request, @PathVariable String language) throws Exception {
        return openApiResource.openapiJson(request, apiDocsUrl, requireSupported(language));
    }

    @GetMapping(path = "/{language}.yaml", produces = "application/vnd.oai.openapi")
    public byte[] yaml(HttpServletRequest request, @PathVariable String language) throws Exception {
        return openApiResource.openapiYaml(request, apiDocsUrl, requireSupported(language));
    }

    private Locale requireSupported(String languageTag) {
        return SupportedLanguage.fromTag(languageTag)
                .map(SupportedLanguage::locale)
                .orElseThrow(() -> new ApiException.UnsupportedLanguage(supportedTags()));
    }

    private static String supportedTags() {
        return Arrays.stream(SupportedLanguage.values())
                .map(SupportedLanguage::tag)
                .collect(Collectors.joining(", "));
    }
}
