package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.support.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BoletoApiTest {

    private static final String PATH = "/v1/boletos";

    private static final String PAYER = """
            "payer": {
              "name": "Ana Paula Souza",
              "taxId": { "value": "12345678901" },
              "postalCode": "01310-100"
            }""";

    private static final String SIMPLE = """
            {
              "category": "SIMPLE",
              %s,
              "amount": { "amount": 149.90, "currency": "BRL" },
              "dueDate": "2027-12-05"
            }""".formatted(PAYER);

    private static final String HYBRID = """
            {
              "category": "HYBRID",
              %s,
              "amount": { "amount": 149.90, "currency": "BRL" },
              "dueDate": "2027-12-05",
              "pixKey": "cobranca@example.com",
              "qrCodeValidityMinutes": 60,
              "allowPartialPayment": true
            }""".formatted(PAYER);

    private static final String SPLIT = """
            {
              "category": "SPLIT",
              %s,
              "amount": { "amount": 1000.00, "currency": "BRL" },
              "dueDate": "2027-12-05",
              "settlementMode": "ON_PAYMENT",
              "recipients": [
                { "name": "Seller", "taxId": { "value": "12345678000195" }, "percentage": 70.00 },
                { "name": "Platform", "taxId": { "value": "98765432000110" }, "percentage": 30.00 }
              ]
            }""".formatted(PAYER);

    @LocalServerPort
    private int port;

    private ApiClient api;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
    }

    @Test
    @DisplayName("returns the fewest members for the simple category")
    void issuesSimple() {
        JsonNode body = issued(SIMPLE);

        assertThat(body.path("category").asString()).isEqualTo("SIMPLE");
        assertThat(body.path("status").asString()).isEqualTo("ISSUED");
        assertThat(body.path("digitableLine").asString()).isNotBlank();
        assertThat(body.path("qrCode").isMissingNode()).isTrue();
        assertThat(body.path("splits").isMissingNode()).isTrue();
    }

    @Test
    @DisplayName("adds the QR code object for the hybrid category")
    void issuesHybrid() {
        JsonNode body = issued(HYBRID);

        assertThat(body.path("category").asString()).isEqualTo("HYBRID");
        assertThat(body.path("partialPaymentAllowed").asBoolean()).isTrue();

        JsonNode qrCode = body.path("qrCode");
        assertThat(qrCode.path("payload").asString()).contains("cobranca@example.com");
        assertThat(qrCode.path("imageUri").asString()).contains(body.path("id").asString());
        assertThat(qrCode.path("expiresAt").asString()).isNotBlank();
        assertThat(body.path("splits").isMissingNode()).isTrue();
    }

    @Test
    @DisplayName("computes one monetary allocation per recipient for the split category")
    void issuesSplit() {
        JsonNode body = issued(SPLIT);

        assertThat(body.path("category").asString()).isEqualTo("SPLIT");
        assertThat(body.path("settlementMode").asString()).isEqualTo("ON_PAYMENT");
        assertThat(body.path("qrCode").isMissingNode()).isTrue();

        JsonNode splits = body.path("splits");
        assertThat(splits.size()).isEqualTo(2);
        assertThat(splits.get(0).path("amount").path("amount").asDouble()).isEqualTo(700.00);
        assertThat(splits.get(1).path("amount").path("amount").asDouble()).isEqualTo(300.00);
        assertThat(splits.get(0).path("amount").path("currency").asString()).isEqualTo("BRL");
        assertThat(splits.get(0).path("taxId").path("kind").asString()).isEqualTo("COMPANY");
    }

    @Test
    @DisplayName("returns each slip as the variant it was issued as")
    void retrievesTheIssuedVariant() {
        String location = api.post(PATH, "en-US", HYBRID).header("Location");

        JsonNode retrieved = api.get(location.substring(location.indexOf(PATH)), "en-US").json();

        assertThat(retrieved.path("category").asString()).isEqualTo("HYBRID");
        assertThat(retrieved.path("qrCode").path("payload").asString()).isNotBlank();
    }

    @Test
    @DisplayName("validates the members of the selected variant, not of the base")
    void validatesVariantMembers() {
        ApiClient.Response response = api.post(PATH, "en-US", """
                {
                  "category": "HYBRID",
                  %s,
                  "amount": { "amount": 149.90, "currency": "BRL" },
                  "dueDate": "2027-12-05",
                  "pixKey": "",
                  "qrCodeValidityMinutes": 4000
                }""".formatted(PAYER));

        assertThat(response.status()).isEqualTo(422);
        assertThat(response.json().path("violations").findValuesAsString("pointer"))
                .contains("/pixKey", "/qrCodeValidityMinutes");
    }

    @Test
    @DisplayName("rejects a due date in the past regardless of category")
    void rejectsPastDueDate() {
        ApiClient.Response response = api.post(PATH, "pt-BR", SIMPLE.replace("2027-12-05", "2020-01-01"));

        assertThat(response.status()).isEqualTo(422);
        assertThat(response.json().path("violations").findValuesAsString("pointer")).contains("/dueDate");
    }

    @Test
    @DisplayName("rejects an unknown category as a malformed body")
    void rejectsUnknownCategory() {
        ApiClient.Response response = api.post(PATH, "en-US", SIMPLE.replace("SIMPLE", "DEPOSIT"));

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.json().path("code").asString()).isEqualTo("MALFORMED_REQUEST");
    }

    private JsonNode issued(String payload) {
        ApiClient.Response response = api.post(PATH, "en-US", payload);
        assertThat(response.status())
                .withFailMessage("issuance failed: %s", response.body())
                .isEqualTo(201);
        return response.json();
    }
}
