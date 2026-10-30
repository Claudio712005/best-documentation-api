package com.clau.best.documentation.api.shared.api;

/**
 * Header names that are part of the published contract.
 *
 * <p>Centralized because each of these is referenced in three places — the controller signature, the
 * OpenAPI documentation annotation, and the test asserting it. A typo in any one of them produces a
 * document that disagrees with the implementation, which is worse than no document.
 */
public final class ApiHeaders {

    /** Standard content negotiation of the response language (RFC 9110 §12.5.4). */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    /** Language actually served, echoed on every response so caches key on it correctly. */
    public static final String CONTENT_LANGUAGE = "Content-Language";

    /** Caller-supplied correlation id. Echoed back, and quoted inside every problem payload. */
    public static final String REQUEST_ID = "X-Request-Id";

    /** Caller-supplied key making an unsafe operation replay-safe (IETF idempotency-key draft). */
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    /** Remaining quota in the current rate-limit window (IETF ratelimit-headers draft). */
    public static final String RATE_LIMIT_REMAINING = "RateLimit-Remaining";

    private ApiHeaders() {
    }
}
