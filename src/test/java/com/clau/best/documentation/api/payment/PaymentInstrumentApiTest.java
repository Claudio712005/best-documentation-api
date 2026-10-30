package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.support.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentInstrumentApiTest {

    private static final String PATH = "/v1/payment-instruments";

    private static final String CARD = """
            {
              "type": "CARD",
              "label": "Personal card",
              "holderName": "ANA P SOUZA",
              "preferred": true,
              "number": "4111111111111111",
              "expiryMonth": 9,
              "expiryYear": 2031,
              "securityCode": "123"
            }""";

    @LocalServerPort
    private int port;

    private ApiClient api;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
    }

    @Test
    @DisplayName("registers a variant and answers 201 with a Location header")
    void registersVariant() {
        ApiClient.Response response = api.post(PATH, "pt-BR", CARD);

        assertThat(response.status()).isEqualTo(201);
        assertThat(response.header("Location")).contains(PATH + "/");

        assertThat(response.header("Content-Language")).isEqualTo("pt-BR");

        JsonNode body = response.json();
        assertThat(body.path("id").asString()).isNotBlank();
        assertThat(body.path("registeredAt").asString())
                .withFailMessage("timestamps must serialize as ISO-8601, not as epoch numbers")
                .contains("T");
        assertThat(body.path("instrument").path("type").asString()).isEqualTo("CARD");
    }

    @Test
    @DisplayName("never returns the write-only members it accepted")
    void hidesWriteOnlyMembers() {
        String location = api.post(PATH, "en-US", CARD).header("Location");
        JsonNode instrument = api.get(path(location), "en-US").json().path("instrument");

        assertThat(instrument.path("number").isMissingNode()).isTrue();
        assertThat(instrument.path("securityCode").isMissingNode()).isTrue();

        assertThat(instrument.path("type").asString()).isEqualTo("CARD");
        assertThat(instrument.path("expiryMonth").asInt()).isEqualTo(9);
    }

    @Test
    @DisplayName("deserializes every variant by its discriminator")
    void deserializesEveryVariant() {
        List<String> payloads = List.of(
                CARD,
                """
                {"type":"PIX","label":"Pix","holderName":"ANA P SOUZA",
                 "keyType":"EMAIL","key":"ana.souza@example.com"}""",
                """
                {"type":"BOLETO","label":"Slip","holderName":"ANA P SOUZA",
                 "taxId":"12345678901","postalCode":"01310-100"}""",
                """
                {"type":"WALLET","label":"Wallet","holderName":"ANA P SOUZA",
                 "provider":"APPLE_PAY","accountReference":"ana@example.com",
                 "deviceToken":"tok_1Nv8xQ2eZvKYlo2C"}""");

        for (String payload : payloads) {
            ApiClient.Response response = api.post(PATH, "en-US", payload);
            assertThat(response.status())
                    .withFailMessage("rejected a valid variant: %s", response.body())
                    .isEqualTo(201);
        }
    }

    @Test
    @DisplayName("answers an unknown identifier with a localized problem payload")
    void reportsUnknownIdentifier() {
        String unknown = PATH + "/" + UUID.randomUUID();

        ApiClient.Response portuguese = api.get(unknown, "pt-BR");
        ApiClient.Response spanish = api.get(unknown, "es-ES");

        assertThat(portuguese.status()).isEqualTo(404);
        assertThat(portuguese.contentType()).startsWith("application/problem+json");

        JsonNode problem = portuguese.json();
        assertThat(problem.path("code").asString()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(problem.path("status").asInt()).isEqualTo(404);
        assertThat(problem.path("instance").asString()).isEqualTo(unknown);
        assertThat(problem.path("traceId").asString()).isNotBlank();
        assertThat(problem.path("type").asString()).contains("/problems/resource-not-found");

        assertThat(problem.path("title").asString())
                .isNotEqualTo(spanish.json().path("title").asString());
    }

    @Test
    @DisplayName("reports every constraint violation at once, located by JSON Pointer")
    void reportsAllViolations() {
        ApiClient.Response response = api.post(PATH, "en-US", """
                {
                  "type": "CARD",
                  "label": "",
                  "holderName": "ANA P SOUZA",
                  "number": "41111",
                  "expiryMonth": 13,
                  "expiryYear": 2031,
                  "securityCode": "1"
                }""");

        assertThat(response.status()).isEqualTo(422);
        assertThat(response.contentType()).startsWith("application/problem+json");

        JsonNode problem = response.json();
        assertThat(problem.path("code").asString()).isEqualTo("VALIDATION_FAILED");

        List<String> pointers = problem.path("violations").findValuesAsString("pointer");
        assertThat(pointers)
                .withFailMessage("expected one violation per failed member, got %s", pointers)
                .contains("/label", "/number", "/expiryMonth", "/securityCode");
        assertThat(problem.path("violations").findValuesAsString("message"))
                .allMatch(message -> !message.isBlank());
    }

    @Test
    @DisplayName("rejects an unknown discriminator value as a malformed body")
    void rejectsUnknownVariant() {
        ApiClient.Response response = api.post(PATH, "en-US", """
                {"type":"CRYPTO","label":"x","holderName":"y"}""");

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.json().path("code").asString()).isEqualTo("MALFORMED_REQUEST");
    }

    private static String path(String location) {
        return location.substring(location.indexOf(PATH));
    }
}
