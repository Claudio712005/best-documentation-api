package com.clau.best.documentation.api.shared.doc;

import com.clau.best.documentation.api.shared.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares {@code 404} — only on operations that actually address a resource by identifier. */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "404",
        description = "doc.response.404.description",
        content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ApiProblem.class)))
public @interface NotFoundResponse {
}
