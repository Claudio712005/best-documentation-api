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
@Schema(name = "FieldViolation", description = "doc.schema.FieldViolation.description")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldViolation(

        @Schema(
                description = "doc.schema.FieldViolation.pointer",
                examples = "/items/0/quantity",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String pointer,

        @Schema(
                description = "doc.schema.FieldViolation.constraint",
                examples = {"Min", "NotBlank", "Pattern"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String constraint,

        @Schema(
                description = "doc.schema.FieldViolation.message",
                examples = "must be greater than or equal to 1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(description = "doc.schema.FieldViolation.rejectedValue", examples = "0")
        Object rejectedValue) {

    public static FieldViolation of(String pointer, String constraint, String message) {
        return new FieldViolation(pointer, constraint, message, null);
    }
}
