package com.clau.best.documentation.api.shared.doc;

import com.clau.best.documentation.api.shared.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The failure responses every operation in this API can produce.
 *
 * <p>Collected into one meta-annotation for two reasons. The obvious one is volume: six responses
 * repeated across every operation is hundreds of lines that no reviewer will read. The subtler one
 * is honesty — once declaring an error costs nothing, operations stop quietly omitting the ones
 * they actually return, which is the most common lie in a generated document.
 *
 * <p>Status-specific responses such as {@code 404} and {@code 409} are intentionally left out: they
 * are declared per operation, since claiming an operation can answer {@code 404} when it addresses
 * no resource is just as misleading as omitting one that can.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "doc.response.400.description",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiProblem.class))),
        @ApiResponse(
                responseCode = "406",
                description = "doc.response.406.description",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiProblem.class))),
        @ApiResponse(
                responseCode = "422",
                description = "doc.response.422.description",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiProblem.class))),
        @ApiResponse(
                responseCode = "500",
                description = "doc.response.500.description",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ApiProblem.class)))
})
public @interface StandardErrorResponses {
}
