package com.clau.best.documentation.api.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

/**
 * Catalogue of machine-readable error codes.
 *
 * <p>HTTP status codes are too coarse for clients to branch on: three unrelated failures all answer
 * {@code 422}. The {@code code} carried in the problem payload is the stable contract, the status is
 * just transport. Codes are therefore append-only — renaming one is a breaking change.
 *
 * <p>Each constant resolves two message keys, {@code problem.<code>.title} and
 * {@code problem.<code>.detail}, from the runtime bundle, so every error is localized.
 *
 * <p>{@code enumAsRef = true} is what makes this appear in {@code components.schemas} instead of
 * being inlined at each use site. Inlined, the value list is repeated in every schema that
 * references it, this description is dropped, and client generators emit one anonymous enum per
 * occurrence rather than a single shared type.
 */
@Schema(
        name = "ApiErrorCode",
        description = "doc.schema.ApiErrorCode.description",
        examples = "VALIDATION_FAILED",
        enumAsRef = true)
public enum ApiErrorCode {

    /** Request body or parameters violated a declared constraint. */
    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "validation-failed"),

    /** Body could not be parsed at all — malformed JSON, wrong type, unreadable payload. */
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "malformed-request"),

    /** A path or query parameter was absent, or present with an unusable value. */
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "invalid-parameter"),

    /** Referenced resource does not exist, or is not visible to the caller. */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "resource-not-found"),

    /** Request was valid but conflicts with the current state of the resource. */
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "resource-conflict"),

    /** The {@code Accept-Language} offered no language this API can serve. */
    UNSUPPORTED_LANGUAGE(HttpStatus.NOT_ACCEPTABLE, "unsupported-language"),

    /** Monetary values in the same request disagreed on currency. */
    CURRENCY_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "currency-mismatch"),

    /** The payment instrument exists but cannot be used for this operation. */
    PAYMENT_INSTRUMENT_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY, "payment-instrument-rejected"),

    /** An {@code Idempotency-Key} was replayed with a different payload. */
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "idempotency-conflict"),

    /** Unexpected server-side failure. Never carries domain detail. */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error");

    private final HttpStatus status;
    private final String slug;

    ApiErrorCode(HttpStatus status, String slug) {
        this.status = status;
        this.slug = slug;
    }

    public HttpStatus status() {
        return status;
    }

    /** Last segment of the {@code type} URI, which dereferences to human documentation. */
    public String slug() {
        return slug;
    }

    public String titleKey() {
        return "problem." + name() + ".title";
    }

    public String detailKey() {
        return "problem." + name() + ".detail";
    }
}
