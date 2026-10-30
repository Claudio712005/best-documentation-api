package com.clau.best.documentation.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

/**
 * Everything about the published contract that is environment-dependent rather than code.
 *
 * <p>Server URLs, the contact e-mail and the documentation base URI change between environments and
 * must never be hard-coded into annotations — an annotation cannot be overridden per profile, which
 * is how OpenAPI documents end up advertising a localhost server in production.
 *
 * <p>Validated at startup: a malformed contract definition fails the boot rather than silently
 * publishing a broken document.
 */
@Validated
@ConfigurationProperties(prefix = "api")
public record ApiProperties(

        @NotBlank String title,

        @NotBlank String version,

        /**
         * Base URI that every RFC 9457 {@code type} is resolved against. Each
         * {@link com.clau.best.documentation.api.shared.error.ApiErrorCode} slug is appended to it,
         * and the resulting URI is expected to dereference to human-readable documentation.
         */
        @NotNull URI problemBaseUri,

        @NotNull URI documentationUri,

        @NotNull Contact contact,

        @NotNull License license,

        @NotEmpty List<Server> servers) {

    /** Who to reach when the contract is wrong. Required by the API style guide. */
    public record Contact(@NotBlank String name, @NotBlank String email, @NotNull URI url) {
    }

    public record License(@NotBlank String name, @NotNull URI url) {
    }

    /**
     * A deployment the document is valid against.
     *
     * @param description shown verbatim in the server picker of the documentation UI
     */
    public record Server(@NotBlank String url, @NotBlank String description) {
    }

    /** Resolves the {@code type} URI for one error code, e.g. {@code .../problems/not-found}. */
    public URI problemType(String slug) {
        String base = problemBaseUri.toString();
        return URI.create(base.endsWith("/") ? base + slug : base + "/" + slug);
    }
}
