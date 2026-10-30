package com.clau.best.documentation.api.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "Problem", description = "doc.schema.Problem.description")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblem(

        @Schema(
                description = "doc.schema.Problem.type",
                format = "uri",
                examples = "https://docs.best-documentation-api.dev/problems/validation-failed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI type,

        @Schema(
                description = "doc.schema.Problem.title",
                examples = "Validation failed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(
                description = "doc.schema.Problem.status",
                examples = "422",
                minimum = "400",
                maximum = "599",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int status,

        @Schema(
                description = "doc.schema.Problem.detail",
                examples = "The request body failed 2 constraint(s). See \"violations\" for the exact locations.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String detail,

        @Schema(
                description = "doc.schema.Problem.instance",
                format = "uri-reference",
                examples = "/v1/orders",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI instance,

        @Schema(
                description = "doc.schema.Problem.code",
                requiredMode = Schema.RequiredMode.REQUIRED)
        ApiErrorCode code,

        @Schema(
                description = "doc.schema.Problem.traceId",
                examples = "01JC8Z5W9QK3MV2T7X4YB6NDHA",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String traceId,

        @Schema(
                description = "doc.schema.Problem.timestamp",
                format = "date-time",
                examples = "2026-10-30T20:14:05.182-03:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime timestamp,

        @Schema(description = "doc.schema.Problem.violations")
        List<FieldViolation> violations) {
}
