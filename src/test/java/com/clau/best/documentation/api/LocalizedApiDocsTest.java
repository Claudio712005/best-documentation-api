package com.clau.best.documentation.api;

import com.clau.best.documentation.api.support.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalizedApiDocsTest {

    private static final String BASE = "/v3/api-docs/language/";

    @LocalServerPort
    private int port;

    private ApiClient api;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
    }

    @ParameterizedTest(name = "{0} is served regardless of Accept-Language")
    @ValueSource(strings = {"en-US", "pt-BR", "es-ES"})
    @DisplayName("serves each language at its own URL, overriding content negotiation")
    void servesEachLanguageAtItsOwnUrl(String languageTag) {
        ApiClient.Response response = api.get(BASE + languageTag, "ja-JP");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.json().path("openapi").asString()).isEqualTo("3.1.0");

        String requested = response.json()
                .path("paths").path("/v1/payment-instruments").path("post")
                .path("summary").asString();
        String negotiated = api.get("/v3/api-docs", languageTag).json()
                .path("paths").path("/v1/payment-instruments").path("post")
                .path("summary").asString();

        assertThat(requested).isNotBlank().isEqualTo(negotiated);
    }

    @Test
    @DisplayName("each language URL yields different prose")
    void languagesDifferFromEachOther() {
        assertThat(summary("en-US")).isNotEqualTo(summary("pt-BR"));
        assertThat(summary("pt-BR")).isNotEqualTo(summary("es-ES"));
    }

    @Test
    @DisplayName("keeps the language endpoints out of the published document")
    void hidesItselfFromTheDocument() {
        assertThat(api.get("/v3/api-docs", "en-US").body()).doesNotContain("/v3/api-docs/language");
    }

    @Test
    @DisplayName("advertises every language in swagger-config, which drives the UI selector")
    void advertisesLanguagesToTheUi() {
        ApiClient.Response response = api.get("/v3/api-docs/swagger-config", "en-US");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.json().path("urls").findValuesAsString("url"))
                .containsExactlyInAnyOrder(
                        BASE + "en-US", BASE + "pt-BR", BASE + "es-ES");
        assertThat(response.json().path("urls").findValuesAsString("name"))
                .hasSize(3)
                .allMatch(name -> !name.isBlank());
    }

    @Test
    @DisplayName("rejects an unsupported language with a localized problem")
    void rejectsUnsupportedLanguage() {
        ApiClient.Response response = api.get(BASE + "ja-JP", "pt-BR");

        assertThat(response.status()).isEqualTo(406);
        assertThat(response.contentType()).startsWith("application/problem+json");
        assertThat(response.json().path("code").asString()).isEqualTo("UNSUPPORTED_LANGUAGE");
        assertThat(response.json().path("detail").asString()).contains("en-US", "pt-BR", "es-ES");
    }

    private String summary(String languageTag) {
        return api.get(BASE + languageTag, "en-US").json()
                .path("info").path("summary").asString();
    }
}
