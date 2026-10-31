package com.clau.best.documentation.api.shared.error;

import java.util.List;

public abstract class ApiException extends RuntimeException {

    private final ApiErrorCode code;
    private final transient List<Object> detailArgs;

    protected ApiException(ApiErrorCode code, Object... detailArgs) {
        super(code.name());
        this.code = code;
        this.detailArgs = List.of(detailArgs);
    }

    public ApiErrorCode code() {
        return code;
    }

    public Object[] detailArgs() {
        return detailArgs.toArray();
    }

    public static class ResourceNotFound extends ApiException {
        public ResourceNotFound(String resource, Object identifier) {
            super(ApiErrorCode.RESOURCE_NOT_FOUND, resource, identifier);
        }
    }

    public static class ResourceConflict extends ApiException {
        public ResourceConflict(String resource) {
            super(ApiErrorCode.RESOURCE_CONFLICT, resource);
        }
    }

    public static class CurrencyMismatch extends ApiException {
        public CurrencyMismatch(String expected, String actual) {
            super(ApiErrorCode.CURRENCY_MISMATCH, expected, actual);
        }
    }

    public static class PaymentInstrumentRejected extends ApiException {
        public PaymentInstrumentRejected(Object instrumentId) {
            super(ApiErrorCode.PAYMENT_INSTRUMENT_REJECTED, instrumentId);
        }
    }

    public static class IdempotencyConflict extends ApiException {
        public IdempotencyConflict(String key) {
            super(ApiErrorCode.IDEMPOTENCY_CONFLICT, key);
        }
    }

    public static class UnsupportedLanguage extends ApiException {
        public UnsupportedLanguage(String supportedTags) {
            super(ApiErrorCode.UNSUPPORTED_LANGUAGE, supportedTags);
        }
    }
}
