package com.clau.best.documentation.api.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Minimal HTTP client for end-to-end tests.
 *
 * <p>Real requests over the loopback interface rather than {@code MockMvc}, because several of the
 * behaviours under test live outside the handler: locale negotiation happens in a
 * {@code LocaleResolver}, {@code Content-Language} is written by an interceptor, and the error
 * payload is produced by an exception handler and serialized by the configured message converters.
 * A test that short-circuits the servlet stack verifies the controller and misses all of it.
 */
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

    /** One response, with the body parsed lazily so a non-JSON failure still reports its text. */
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
