package com.clau.best.documentation.api.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ApiClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final HttpClient http = HttpClient.newHttpClient();
    private final int port;

    public ApiClient(int port) {
        this.port = port;
    }

    public Response get(String path, String languageTag) {
        return send(HttpRequest.newBuilder(uri(path))
                .header("Accept-Language", languageTag)
                .GET());
    }

    public Response post(String path, String languageTag, String body) {
        return send(HttpRequest.newBuilder(uri(path))
                .header("Accept-Language", languageTag)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)));
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private Response send(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response =
                    http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return new Response(response.statusCode(), response, response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Request failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during request", exception);
        }
    }

    public record Response(int status, HttpResponse<String> raw, String body) {

        public JsonNode json() {
            return MAPPER.readTree(body);
        }

        public String header(String name) {
            return raw.headers().firstValue(name).orElse(null);
        }

        public String contentType() {
            return header("Content-Type");
        }
    }
}
