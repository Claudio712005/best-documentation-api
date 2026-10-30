package com.clau.best.documentation.api.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches the generated OpenAPI document over HTTP and keeps a copy on disk.
 *
 * <p>Deliberately goes through the real endpoint rather than invoking springdoc's services
 * directly: the locale is negotiated by an MVC {@code LocaleResolver} and the document is cached
 * per locale, so a test that bypasses the request pipeline would verify generation while proving
 * nothing about what clients actually receive.
 *
 * <p>Uses the JDK HTTP client so that nothing about this fixture depends on which test-support
 * abstractions a given Spring Boot generation happens to ship.
 */
public final class OpenApiDocumentFixture {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final Path OUTPUT_DIRECTORY = Path.of("target", "openapi");

    private OpenApiDocumentFixture() {
    }

    /**
     * Requests {@code /v3/api-docs} in one language.
     *
     * @param languageTag value sent in {@code Accept-Language}
     * @return the parsed document, also written to {@code target/openapi} for inspection
     */
    public static JsonNode fetch(int port, String languageTag) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:" + port + "/v3/api-docs"))
                            .header("Accept-Language", languageTag)
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "GET /v3/api-docs answered " + response.statusCode() + ": " + response.body());
            }

            Files.createDirectories(OUTPUT_DIRECTORY);
            Files.writeString(OUTPUT_DIRECTORY.resolve("openapi-" + languageTag + ".json"), response.body());
            return MAPPER.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not fetch the OpenAPI document", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching the OpenAPI document", exception);
        }
    }

    /**
     * Collects every string in the document that still looks like an unresolved message key.
     *
     * <p>This is the one assertion that keeps the translations honest as the API grows: a key added
     * to an annotation but forgotten in a bundle resolves to itself, which reads as a plausible
     * value and would otherwise ship unnoticed.
     *
     * @return JSON Pointer paths of every value matching the message-key naming convention
     */
    public static List<String> unresolvedMessageKeys(JsonNode document, String keyPrefix) {
        List<String> found = new ArrayList<>();
        collectUnresolved(document, "", keyPrefix, found);
        return found;
    }

    private static void collectUnresolved(
            JsonNode node, String pointer, String keyPrefix, List<String> found) {

        if (node.isString()) {
            if (node.stringValue().startsWith(keyPrefix)) {
                found.add(pointer + " = " + node.stringValue());
            }
            return;
        }
        if (node.isObject()) {
            node.properties().forEach(entry ->
                    collectUnresolved(entry.getValue(), pointer + "/" + entry.getKey(), keyPrefix, found));
            return;
        }
        if (node.isArray()) {
            for (int index = 0; index < node.size(); index++) {
                collectUnresolved(node.get(index), pointer + "/" + index, keyPrefix, found);
            }
        }
    }
}
