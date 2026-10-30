package com.clau.best.documentation.api.shared.error;

import com.clau.best.documentation.api.config.ApiProperties;
import com.clau.best.documentation.api.shared.api.ApiHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Single place where an {@link ApiProblem} is built.
 *
 * <p>Localization happens here and nowhere else: exceptions carry a code and interpolation
 * arguments, and only this factory knows the caller's {@link Locale}. That split is what keeps the
 * service layer free of presentation concerns while still answering in the caller's language.
 */
@Component
public class ProblemFactory {

    private static final String TRACE_ID_ATTRIBUTE = ProblemFactory.class.getName() + ".traceId";

    private final ApiProperties apiProperties;
    private final MessageSource messageSource;

    public ProblemFactory(ApiProperties apiProperties, MessageSource messageSource) {
        this.apiProperties = apiProperties;
        this.messageSource = messageSource;
    }

    public ApiProblem create(ApiErrorCode code, HttpServletRequest request, Object... detailArgs) {
        return create(code, request, null, detailArgs);
    }

    public ApiProblem create(
            ApiErrorCode code,
            HttpServletRequest request,
            List<FieldViolation> violations,
            Object... detailArgs) {

        Locale locale = LocaleContextHolder.getLocale();
        return new ApiProblem(
                apiProperties.problemType(code.slug()),
                message(code.titleKey(), locale),
                code.status().value(),
                message(code.detailKey(), locale, detailArgs),
                URI.create(request.getRequestURI()),
                code,
                traceId(request),
                OffsetDateTime.now(),
                violations == null || violations.isEmpty() ? null : violations);
    }

    /**
     * Resolves a localized message, falling back to the key itself.
     *
     * <p>Returning the key rather than throwing is deliberate: a missing translation should degrade
     * one string, not turn a {@code 404} into a {@code 500}.
     */
    private String message(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }

    /**
     * Correlation identifier for this request: the caller's own if supplied, otherwise a fresh one.
     *
     * <p>Memoized on the request so that logging the identifier and returning it to the client
     * cannot drift apart — generating it twice would hand the caller a value absent from the logs,
     * which defeats the entire point of the header.
     */
    public String traceId(HttpServletRequest request) {
        Object cached = request.getAttribute(TRACE_ID_ATTRIBUTE);
        if (cached instanceof String traceId) {
            return traceId;
        }
        String supplied = request.getHeader(ApiHeaders.REQUEST_ID);
        String traceId = supplied == null || supplied.isBlank()
                ? UUID.randomUUID().toString()
                : supplied;
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        return traceId;
    }
}
