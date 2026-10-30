package com.clau.best.documentation.api;

import com.clau.best.documentation.api.config.OpenApiConfig;
import com.clau.best.documentation.api.support.OpenApiDocumentFixture;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTest {

    private static final String MESSAGE_KEY_PREFIX = "doc.";

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("is emitted as OpenAPI 3.1 and declares the JSON Schema dialect")
    void declaresVersionAndDialect() {
        JsonNode document = OpenApiDocumentFixture.fetch(port, "en-US");

        assertThat(document.path("openapi").asString()).isEqualTo("3.1.0");

        assertThat(document.path("jsonSchemaDialect").asString())
                .isEqualTo(OpenApiConfig.JSON_SCHEMA_DIALECT);
    }

    @ParameterizedTest(name = "no message key leaks into the {0} document")
    @ValueSource(strings = {"en-US", "pt-BR", "es-ES"})
    @DisplayName("resolves every message key in every supported language")
    void leaksNoMessageKeys(String languageTag) {
        JsonNode document = OpenApiDocumentFixture.fetch(port, languageTag);

        List<String> unresolved =
                OpenApiDocumentFixture.unresolvedMessageKeys(document, MESSAGE_KEY_PREFIX);

        assertThat(unresolved)
                .withFailMessage(
                        "The %s document still carries unresolved message keys, which means these "
                                + "keys are missing from the bundle:%n  %s",
                        languageTag, String.join("%n  ".formatted(), unresolved))
                .isEmpty();
    }

    @Test
    @DisplayName("translates document metadata, tags and operations per Accept-Language")
    void translatesDocumentPerLanguage() {
        JsonNode english = OpenApiDocumentFixture.fetch(port, "en-US");
        JsonNode portuguese = OpenApiDocumentFixture.fetch(port, "pt-BR");
        JsonNode spanish = OpenApiDocumentFixture.fetch(port, "es-ES");

        assertThat(List.of(
                english.path("info").path("summary").asString(),
                portuguese.path("info").path("summary").asString(),
                spanish.path("info").path("summary").asString()))
                .doesNotHaveDuplicates();

        assertThat(tagDescription(english, OpenApiConfig.TAG_SYSTEM))
                .isNotEqualTo(tagDescription(portuguese, OpenApiConfig.TAG_SYSTEM));

        assertThat(healthOperation(english).path("summary").asString())
                .isNotEqualTo(healthOperation(portuguese).path("summary").asString());
    }

    @Test
    @DisplayName("translates component schema descriptions, which springdoc alone does not")
    void translatesSchemaDescriptions() {

        String english = schemaDescription(OpenApiDocumentFixture.fetch(port, "en-US"), "Health");
        String portuguese = schemaDescription(OpenApiDocumentFixture.fetch(port, "pt-BR"), "Health");

        assertThat(english).isNotBlank().isNotEqualTo(portuguese);
        assertThat(portuguese).isNotBlank();
    }

    @Test
    @DisplayName("applies documentation meta-annotations to every operation")
    void appliesMetaAnnotations() {
        JsonNode operation = healthOperation(OpenApiDocumentFixture.fetch(port, "en-US"));

        assertThat(operation.path("operationId").asString()).isEqualTo("getHealth");

        List<String> parameterNames = operation.path("parameters").findValuesAsString("name");
        assertThat(parameterNames).contains("Accept-Language", "X-Request-Id");

        assertThat(operation.path("responses").propertyNames())
                .contains("200", "400", "406", "422", "500");
    }

    @Test
    @DisplayName("declares every tag explicitly instead of deriving them from class names")
    void declaresTagsExplicitly() {
        JsonNode document = OpenApiDocumentFixture.fetch(port, "en-US");

        List<String> declared = document.path("tags").findValuesAsString("name");

        assertThat(declared).isEqualTo(OpenApiConfig.TAG_ORDER);
        assertThat(declared).noneMatch(tag -> tag.endsWith("-controller"));
    }

    @Test
    @DisplayName("publishes enums as referenced components rather than inlining them")
    void publishesEnumsAsComponents() {
        JsonNode document = OpenApiDocumentFixture.fetch(port, "en-US");
        JsonNode schemas = document.path("components").path("schemas");

        assertThat(schemas.propertyNames()).contains("ApiErrorCode", "HealthStatus");
        assertThat(schemas.path("ApiErrorCode").path("description").asString()).isNotBlank();

        JsonNode code = schemas.path("Problem").path("properties").path("code");
        assertThat(code.path("$ref").asString()).isEqualTo("#/components/schemas/ApiErrorCode");

        assertThat(code.path("description").asString()).isNotBlank();
        assertThat(code.path("enum").isMissingNode()).isTrue();
    }

    private static String tagDescription(JsonNode document, String tagName) {
        for (JsonNode tag : document.path("tags")) {
            if (tagName.equals(tag.path("name").asString())) {
                return tag.path("description").asString();
            }
        }
        throw new AssertionError("Tag not declared in the document: " + tagName);
    }

    private static String schemaDescription(JsonNode document, String schemaName) {
        return document.path("components").path("schemas").path(schemaName).path("description").asString();
    }

    private static JsonNode healthOperation(JsonNode document) {
        return document.path("paths").path("/v1/health").path("get");
    }
}
