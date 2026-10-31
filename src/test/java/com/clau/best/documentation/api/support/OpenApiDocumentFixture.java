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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OpenApiDocumentFixture {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final Path OUTPUT_DIRECTORY = Path.of("target", "openapi");

    private OpenApiDocumentFixture() {
    }

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

    public static List<String> danglingReferences(JsonNode document) {
        Set<String> defined = new HashSet<>();
        JsonNode schemas = document.path("components").path("schemas");
        schemas.properties().forEach(entry -> defined.add("#/components/schemas/" + entry.getKey()));

        List<String> dangling = new ArrayList<>();
        collectReferences(document, "", defined, dangling);
        return dangling;
    }

    private static void collectReferences(
            JsonNode node, String pointer, Set<String> defined, List<String> dangling) {

        if (node.isObject()) {
            JsonNode reference = node.path("$ref");
            if (reference.isString()
                    && reference.stringValue().startsWith("#/components/schemas/")
                    && !defined.contains(reference.stringValue())) {

                dangling.add(pointer + " -> " + reference.stringValue());
            }
            node.properties().forEach(entry ->
                    collectReferences(entry.getValue(), pointer + "/" + entry.getKey(), defined, dangling));
            return;
        }
        if (node.isArray()) {
            for (int index = 0; index < node.size(); index++) {
                collectReferences(node.get(index), pointer + "/" + index, defined, dangling);
            }
        }
    }
}
