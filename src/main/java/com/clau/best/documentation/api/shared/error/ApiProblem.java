package com.clau.best.documentation.api.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Error payload, shaped by RFC 9457 (Problem Details for HTTP APIs).
 *
 * <p>Served as {@code application/problem+json}. The five RFC members are kept verbatim so generic
 * tooling understands the payload; everything this API adds ({@code code}, {@code traceId},
 * {@code timestamp}, {@code violations}) is an RFC extension member, which the spec explicitly
 * allows and which clients may ignore.
 *
 * <p>One schema for every failure, in every endpoint: documenting a different error shape per route
 * is the single most common way an OpenAPI document rots.
 */
@Schema(
        name = "Problem",
        description = """
                Error payload following RFC 9457 (Problem Details for HTTP APIs). Served with the \
                `application/problem+json` media type by every failing operation in this API.""")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblem(

        @Schema(
                description = """
                        URI identifying the problem type. Dereferences to documentation describing \
                        the failure and how to recover from it.""",
                example = "https://docs.best-documentation-api.dev/problems/validation-failed",
                format = "uri",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI type,

        @Schema(
                description = """
                        Short, human-readable summary of the problem type, localized through \
                        `Accept-Language`. Stable for a given `type` — never interpolated with \
                        request data, so it stays safe to use as a log key.""",
                example = "Validation failed",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(
                description = "HTTP status code, repeated here so the payload is self-contained.",
                example = "422",
                minimum = "400",
                maximum = "599",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int status,

        @Schema(
                description = """
                        Explanation specific to this occurrence, localized. May interpolate request \
                        data, so it must not be parsed by clients.""",
                example = "The request body failed 2 constraints.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String detail,

        @Schema(
                description = "URI reference identifying the specific occurrence — the request path.",
                example = "/v1/orders",
                format = "uri-reference",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI instance,

        @Schema(
                description = """
                        RFC 9457 extension. Machine-readable error code — the value clients should \
                        branch on.""",
                requiredMode = Schema.RequiredMode.REQUIRED)
        ApiErrorCode code,

        @Schema(
                description = """
                        RFC 9457 extension. Correlation identifier, echoed from the request's \
                        `X-Request-Id` or generated. Quote it in support tickets.""",
                example = "01JC8Z5W9QK3MV2T7X4YB6NDHA",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String traceId,

        @Schema(
                description = "RFC 9457 extension. Server time at which the failure was observed.",
                example = "2026-10-30T20:14:05.182-03:00",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime timestamp,

        @Schema(
                description = """
                        RFC 9457 extension. Present only for `VALIDATION_FAILED`, listing every \
                        constraint that failed rather than just the first one.""")
        List<FieldViolation> violations) {
}
