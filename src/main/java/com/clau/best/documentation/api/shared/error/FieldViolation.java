package com.clau.best.documentation.api.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One failed constraint, addressed by its location in the request.
 *
 * <p>The {@code pointer} is a JSON Pointer (RFC 6901) rather than a dotted property path, so a
 * client can map the violation back onto the exact node it sent — including array indices — without
 * parsing a bespoke format.
 */
@Schema(
        name = "FieldViolation",
        description = "A single constraint violation, located by JSON Pointer within the request.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldViolation(

        @Schema(
                description = "JSON Pointer (RFC 6901) to the offending node in the request body.",
                example = "/items/0/quantity",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String pointer,

        @Schema(
                description = "Identifier of the violated constraint, stable across languages.",
                example = "Min",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String constraint,

        @Schema(
                description = "Localized explanation, honouring the request's `Accept-Language`.",
                example = "must be greater than or equal to 1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(
                description = """
                        The value that was rejected, echoed back for debugging. Omitted when the \
                        value is absent or carries sensitive data.""",
                example = "0")
        Object rejectedValue) {

    public static FieldViolation of(String pointer, String constraint, String message) {
        return new FieldViolation(pointer, constraint, message, null);
    }
}
