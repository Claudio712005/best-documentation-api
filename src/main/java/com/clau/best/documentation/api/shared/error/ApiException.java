package com.clau.best.documentation.api.shared.error;

import java.util.List;

/**
 * Base class for every failure this API reports deliberately.
 *
 * <p>Carries an {@link ApiErrorCode} and the arguments its {@code detail} message interpolates —
 * never a pre-formatted sentence. Formatting is deferred to the exception handler, which is the only
 * place that knows the caller's language.
 */
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

    /** Thrown when a resource addressed by identifier does not exist. */
    public static class ResourceNotFound extends ApiException {
        public ResourceNotFound(String resource, Object identifier) {
            super(ApiErrorCode.RESOURCE_NOT_FOUND, resource, identifier);
        }
    }

    /** Thrown when a request is well-formed but inconsistent with current state. */
    public static class ResourceConflict extends ApiException {
        public ResourceConflict(String resource) {
            super(ApiErrorCode.RESOURCE_CONFLICT, resource);
        }
    }

    /** Thrown when monetary values in one request disagree on currency. */
    public static class CurrencyMismatch extends ApiException {
        public CurrencyMismatch(String expected, String actual) {
            super(ApiErrorCode.CURRENCY_MISMATCH, expected, actual);
        }
    }

    /** Thrown when a payment instrument is known but unusable for the operation at hand. */
    public static class PaymentInstrumentRejected extends ApiException {
        public PaymentInstrumentRejected(Object instrumentId) {
            super(ApiErrorCode.PAYMENT_INSTRUMENT_REJECTED, instrumentId);
        }
    }

    /** Thrown when an idempotency key is replayed with a different payload. */
    public static class IdempotencyConflict extends ApiException {
        public IdempotencyConflict(String key) {
            super(ApiErrorCode.IDEMPOTENCY_CONFLICT, key);
        }
    }

    /** Thrown when no language offered in {@code Accept-Language} can be served. */
    public static class UnsupportedLanguage extends ApiException {
        public UnsupportedLanguage(String supportedTags) {
            super(ApiErrorCode.UNSUPPORTED_LANGUAGE, supportedTags);
        }
    }
}
