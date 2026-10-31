package com.clau.best.documentation.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SwaggerTestDataTransformer extends SwaggerIndexPageTransformer {

    private static final String INDEX_PAGE = "index.html";
    private static final String PANEL_SCRIPT = "/swagger-ui/test-data-panel.js";
    private static final String BODY_CLOSE = "</body>";
    private static final String LABEL_PREFIX = "ui.testdata.";

    private static final List<String> LABEL_KEYS = List.of(
            "title",
            "regenerate",
            "cpf",
            "cnpj",
            "cnpjAlphanumeric",
            "copy",
            "copied",
            "toggleHint",
            "note",
            "selfTestFailed");

    private final MessageSource messageSource;
    private final String panelScript;

    public SwaggerTestDataTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider,
            MessageSource messageSource) throws IOException {

        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
        this.messageSource = messageSource;
        this.panelScript = readPanelScript();
    }

    @Override
    public Resource transform(
            HttpServletRequest request, Resource resource, ResourceTransformerChain chain)
            throws IOException {

        Resource transformed = super.transform(request, resource, chain);
        if (!INDEX_PAGE.equals(resource.getFilename())) {
            return transformed;
        }

        String html = read(transformed);
        if (!html.contains(BODY_CLOSE)) {
            return transformed;
        }

        String injection = """
                <script>window.swaggerTestDataLabels=%s;</script>
                <script>%s</script>
                """.formatted(labelsAsJson(RequestContextUtils.getLocale(request)), panelScript);

        return new TransformedResource(
                resource,
                html.replace(BODY_CLOSE, injection + BODY_CLOSE).getBytes(StandardCharsets.UTF_8));
    }

    private String labelsAsJson(Locale locale) {
        Map<String, String> labels = new LinkedHashMap<>();
        LABEL_KEYS.forEach(key -> labels.put(
                key, messageSource.getMessage(LABEL_PREFIX + key, null, LABEL_PREFIX + key, locale)));
        try {
            return objectMapper.writeValueAsString(labels);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize the test data panel labels", exception);
        }
    }

    private static String read(Resource resource) throws IOException {
        try (InputStream stream = resource.getInputStream()) {
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        }
    }

    private static String readPanelScript() throws IOException {
        try (InputStream stream = SwaggerTestDataTransformer.class.getResourceAsStream(PANEL_SCRIPT)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource " + PANEL_SCRIPT);
            }
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        }
    }
}
