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

    private String message(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }

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
