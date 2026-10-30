package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.support.OpenApiDocumentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asserts the shape of the published payment instrument union.
 *
 * <p>Every assertion here corresponds to something springdoc gets wrong on its own, so this class
 * is really a specification of what {@code DiscriminatedUnionCustomizer} must keep doing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscriminatedUnionContractTest {

    private static final String REF = "#/components/schemas/";

    private static final List<String> VARIANTS =
            List.of("BoletoInstrument", "CardInstrument", "PixInstrument", "WalletInstrument");

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("publishes only the concrete variants, never the base type or springdoc's wrapper")
    void publishesOnlyConcreteVariants() {
        JsonNode schemas = schemas();

        assertThat(schemas.propertyNames()).containsAll(VARIANTS);
        // The base type is an implementation detail: no operation accepts or returns it.
        assertThat(schemas.propertyNames()).doesNotContain("PaymentInstrument");
        // springdoc's polymorphic converter emits this wrapper; the union replaces it inline.
        assertThat(schemas.propertyNames()).doesNotContain("PaymentInstrumentVariant");
    }

    @Test
    @DisplayName("flattens inherited members into each variant, with overrides preserved")
    void flattensInheritedMembers() {
        JsonNode schemas = schemas();

        for (String variant : VARIANTS) {
            JsonNode schema = schemas.path(variant);
            assertThat(schema.path("allOf").isMissingNode())
                    .withFailMessage("%s is still composed with allOf", variant)
                    .isTrue();
            assertThat(schema.path("type").asString())
                    .withFailMessage("%s declares no type", variant)
                    .isEqualTo("object");
            assertThat(schema.path("properties").propertyNames())
                    .withFailMessage("%s is missing the inherited members", variant)
                    .contains("type", "label", "holderName", "preferred");
            assertThat(schema.path("required").toString())
                    .withFailMessage("%s lost the inherited required members", variant)
                    .contains("holderName", "label", "type");
        }

        // preferred is @Nullable on the base; the variant's own nullable form must survive the
        // merge rather than being overwritten by the base's non-null one.
        assertThat(schemas.path("CardInstrument").path("properties").path("preferred")
                .path("type").toString())
                .contains("null");
    }

    @Test
    @DisplayName("documents the request body as a union carrying a complete discriminator mapping")
    void documentsRequestBodyAsDiscriminatedUnion() {
        JsonNode schema = OpenApiDocumentFixture.fetch(port, "en-US")
                .path("paths").path("/v1/payment-instruments").path("post")
                .path("requestBody").path("content").path("application/json").path("schema");

        assertThat(schema.path("$ref").isMissingNode())
                .withFailMessage("the request body still points at a single schema")
                .isTrue();

        List<String> members = schema.path("oneOf").findValuesAsString("$ref");
        assertThat(members).containsExactlyInAnyOrderElementsOf(
                VARIANTS.stream().map(name -> REF + name).toList());

        // Without the mapping a client knows the possible shapes but not how to tell them apart,
        // and has to fall back to trial deserialization.
        JsonNode discriminator = schema.path("discriminator");
        assertThat(discriminator.path("propertyName").asString()).isEqualTo("type");
        for (PaymentInstrumentType type : PaymentInstrumentType.values()) {
            assertThat(discriminator.path("mapping").path(type.name()).asString())
                    .withFailMessage("no discriminator mapping for %s", type)
                    .startsWith(REF);
        }
    }

    @Test
    @DisplayName("rewrites the union at every use site, not just the request body")
    void rewritesEveryUseSite() {
        JsonNode nested = schemas()
                .path("PaymentInstrumentResource").path("properties").path("instrument");

        assertThat(nested.path("oneOf").size()).isEqualTo(VARIANTS.size());
        assertThat(nested.path("discriminator").path("propertyName").asString()).isEqualTo("type");
        // A sibling description next to a union is legal in 3.1 and must not be lost by the rewrite.
        assertThat(nested.path("description").asString()).isNotBlank();
    }

    @Test
    @DisplayName("marks secrets write-only so they appear in requests but never in responses")
    void marksSecretsWriteOnly() {
        JsonNode schemas = schemas();

        assertThat(schemas.path("CardInstrument").path("properties").path("number")
                .path("writeOnly").asBoolean()).isTrue();
        assertThat(schemas.path("CardInstrument").path("properties").path("securityCode")
                .path("writeOnly").asBoolean()).isTrue();
        assertThat(schemas.path("WalletInstrument").path("properties").path("deviceToken")
                .path("writeOnly").asBoolean()).isTrue();
    }

    private JsonNode schemas() {
        return OpenApiDocumentFixture.fetch(port, "en-US").path("components").path("schemas");
    }
}
