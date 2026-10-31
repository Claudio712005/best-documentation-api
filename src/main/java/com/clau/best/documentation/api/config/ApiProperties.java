package com.clau.best.documentation.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "api")
public record ApiProperties(

        @NotBlank String title,

        @NotBlank String version,

        @NotNull URI problemBaseUri,

        @NotNull URI documentationUri,

        @NotNull Contact contact,

        @NotNull License license,

        @NotEmpty List<Server> servers) {

    public record Contact(@NotBlank String name, @NotBlank String email, @NotNull URI url) {
    }

    public record License(@NotBlank String name, @NotNull URI url) {
    }

    public record Server(@NotBlank String url, @NotBlank String description) {
    }

    public URI problemType(String slug) {
        String base = problemBaseUri.toString();
        return URI.create(base.endsWith("/") ? base + slug : base + "/" + slug);
    }
}
