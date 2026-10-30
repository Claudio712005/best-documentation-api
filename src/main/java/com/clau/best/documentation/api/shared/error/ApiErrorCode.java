package com.clau.best.documentation.api.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(
        name = "ApiErrorCode",
        description = "doc.schema.ApiErrorCode.description",
        examples = "VALIDATION_FAILED",
        enumAsRef = true)
public enum ApiErrorCode {

    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "validation-failed"),

    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "malformed-request"),

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "invalid-parameter"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "resource-not-found"),

    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "resource-conflict"),

    UNSUPPORTED_LANGUAGE(HttpStatus.NOT_ACCEPTABLE, "unsupported-language"),

    CURRENCY_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "currency-mismatch"),

    PAYMENT_INSTRUMENT_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY, "payment-instrument-rejected"),

    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "idempotency-conflict"),

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
