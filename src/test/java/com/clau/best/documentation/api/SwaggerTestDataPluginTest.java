package com.clau.best.documentation.api;

import com.clau.best.documentation.api.support.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerTestDataPluginTest {

    private static final String INDEX = "/swagger-ui/index.html";

    @LocalServerPort
    private int port;

    private ApiClient api;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
    }

    @Test
    @DisplayName("injects the generator into the documentation page")
    void injectsTheGenerator() {
        ApiClient.Response response = api.get(INDEX, "en-US");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body())
                .contains("window.swaggerTestDataLabels")
                .contains("generateAlphanumericCnpj")
                .contains("window.swaggerTestDataPlugin");
    }

    @Test
    @DisplayName("leaves springdoc's own transformations untouched")
    void keepsSpringdocConfiguration() {
        String html = api.get(INDEX, "en-US").body();
        String initializer = api.get("/swagger-ui/swagger-initializer.js", "en-US").body();

        assertThat(html)
                .withFailMessage("the transformer dropped the page's own assets")
                .contains("swagger-ui-bundle.js")
                .contains("swagger-initializer.js");

        assertThat(initializer)
                .withFailMessage("springdoc no longer configures the UI, so the transformer broke it")
                .contains("/v3/api-docs/swagger-config")
                .contains("urls.primaryName");
        assertThat(initializer)
                .withFailMessage("the plugin is not registered, so no wrapped component renders")
                .contains("SwaggerUIBundle.plugins.DownloadUrl, window.swaggerTestDataPlugin");
        assertThat(initializer).doesNotContain("petstore.swagger.io");
    }

    @Test
    @DisplayName("localizes the panel labels through Accept-Language")
    void localizesPanelLabels() {
        assertThat(api.get(INDEX, "en-US").body()).contains("Test data");
        assertThat(api.get(INDEX, "pt-BR").body()).contains("Massa de teste");
        assertThat(api.get(INDEX, "es-ES").body()).contains("Datos de prueba");
    }

    @Test
    @DisplayName("declares what each operation captures and fills")
    void declaresTestFlowExtensions() {
        var document = com.clau.best.documentation.api.support.OpenApiDocumentFixture.fetch(port, "en-US");

        assertThat(document.path("paths").path("/v1/boletos").path("post")
                .path("x-captures").path("boletoId").asString()).isEqualTo("$.id");
        assertThat(document.path("paths").path("/v1/boletos/{boletoId}").path("get")
                .path("x-fills").path("boletoId").asString()).isEqualTo("boletoId");
        assertThat(document.path("paths").path("/v1/payment-instruments").path("post")
                .path("x-captures").path("instrumentId").asString()).isEqualTo("$.id");
        assertThat(document.path("paths").path("/v1/payment-instruments/{instrumentId}").path("get")
                .path("x-fills").path("instrumentId").asString()).isEqualTo("instrumentId");
    }

    @Test
    @DisplayName("carries the check-digit vectors the script verifies itself against")
    void carriesSelfTestVectors() {
        String html = api.get(INDEX, "en-US").body();

        assertThat(html)
                .withFailMessage("the script lost its self-test, so a broken generator would be silent")
                .contains("12ABC34501DE35")
                .contains("11222333000181")
                .contains("52998224725");
    }
}
