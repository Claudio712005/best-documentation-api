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
