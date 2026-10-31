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
    private static final String PLUGIN_SCRIPT = "/swagger-ui/test-data-plugin.js";
    private static final String INITIALIZER = "swagger-initializer.js";
    private static final String DEFAULT_PLUGINS = "SwaggerUIBundle.plugins.DownloadUrl";
    private static final String REGISTERED_PLUGINS =
            DEFAULT_PLUGINS + ", window.swaggerTestDataPlugin";
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
    private final String pluginScript;

    public SwaggerTestDataTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider,
            MessageSource messageSource) throws IOException {

        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
        this.messageSource = messageSource;
        this.pluginScript = readPluginScript();
    }

    @Override
    public Resource transform(
            HttpServletRequest request, Resource resource, ResourceTransformerChain chain)
            throws IOException {

        Resource transformed = super.transform(request, resource, chain);
        String filename = resource.getFilename();

        if (INDEX_PAGE.equals(filename)) {
            return withPluginScript(resource, read(transformed), request);
        }
        if (INITIALIZER.equals(filename)) {
            return withRegisteredPlugin(resource, read(transformed));
        }
        return transformed;
    }

    private Resource withPluginScript(Resource resource, String html, HttpServletRequest request) {
        if (!html.contains(BODY_CLOSE)) {
            return new TransformedResource(resource, html.getBytes(StandardCharsets.UTF_8));
        }
        String injection = """
                <script>window.swaggerTestDataLabels=%s;</script>
                <script>%s</script>
                """.formatted(labelsAsJson(RequestContextUtils.getLocale(request)), pluginScript);

        return new TransformedResource(
                resource,
                html.replace(BODY_CLOSE, injection + BODY_CLOSE).getBytes(StandardCharsets.UTF_8));
    }

    private Resource withRegisteredPlugin(Resource resource, String initializer) {
        if (!initializer.contains(DEFAULT_PLUGINS)) {
            throw new IllegalStateException(
                    "Could not register the test data plugin: swagger-initializer.js no longer "
                            + "contains \"" + DEFAULT_PLUGINS + "\", so the plugins array moved and the "
                            + "wrapped components would silently stop rendering.");
        }
        return new TransformedResource(
                resource,
                initializer.replace(DEFAULT_PLUGINS, REGISTERED_PLUGINS).getBytes(StandardCharsets.UTF_8));
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

    private static String readPluginScript() throws IOException {
        try (InputStream stream = SwaggerTestDataTransformer.class.getResourceAsStream(PLUGIN_SCRIPT)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource " + PLUGIN_SCRIPT);
            }
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        }
    }
}
