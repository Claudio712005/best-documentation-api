package com.clau.best.documentation.api.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingMatrixVariableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Translates every exception into a localized {@link ApiProblem}.
 *
 * <p>Two rules drive the design:
 *
 * <ul>
 *   <li><b>One response shape, always.</b> Every handler below returns the same schema, so the
 *       OpenAPI document declares {@code Problem} once and reuses it by reference everywhere.
 *   <li><b>All violations, not the first.</b> A client fixing a form wants every field that failed
 *       in one round trip.
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ProblemFactory problems;
    private final MessageSource messageSource;

    public GlobalExceptionHandler(ProblemFactory problems, MessageSource messageSource) {
        this.problems = problems;
        this.messageSource = messageSource;
    }

    /** Failures the domain raises on purpose, already carrying a code and its message arguments. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiProblem> handleApiException(
            ApiException exception, HttpServletRequest request) {

        return respond(problems.create(exception.code(), request, exception.detailArgs()));
    }

    /** Bean validation on an {@code @RequestBody}. Reports every field that failed. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiProblem> handleBodyValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {

        List<FieldViolation> violations = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            violations.add(new FieldViolation(
                    jsonPointer(error.getField()),
                    error.getCode() == null ? "Invalid" : error.getCode(),
                    error.getDefaultMessage() == null ? "" : error.getDefaultMessage(),
                    error.getRejectedValue()));
        }
        for (ObjectError error : exception.getBindingResult().getGlobalErrors()) {
            violations.add(FieldViolation.of(
                    "",
                    error.getCode() == null ? "Invalid" : error.getCode(),
                    error.getDefaultMessage() == null ? "" : error.getDefaultMessage()));
        }
        return respond(problems.create(
                ApiErrorCode.VALIDATION_FAILED, request, violations, violations.size()));
    }

    /** Bean validation on path variables, query parameters and headers. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiProblem> handleParameterValidation(
            HandlerMethodValidationException exception, HttpServletRequest request) {

        Locale locale = LocaleContextHolder.getLocale();
        List<FieldViolation> violations = new ArrayList<>();
        exception.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> violations.add(new FieldViolation(
                    jsonPointer(name == null ? "" : name),
                    constraintOf(error),
                    messageSource.getMessage(error, locale),
                    result.getArgument())));
        });
        return respond(problems.create(
                ApiErrorCode.VALIDATION_FAILED, request, violations, violations.size()));
    }

    /** Body that Jackson could not even read: broken JSON, wrong type, unknown enum value. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiProblem> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {

        log.debug("Unreadable request body on {}", request.getRequestURI(), exception);
        return respond(problems.create(ApiErrorCode.MALFORMED_REQUEST, request));
    }

    /**
     * A required request value was absent, or present with a value of the wrong type.
     *
     * <p>Catching the {@link MissingRequestValueException} base type covers query parameters,
     * headers, cookies, path and matrix variables in one handler; only the accessor exposing the
     * offending name differs between them.
     */
    @ExceptionHandler({MissingRequestValueException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiProblem> handleBadRequestValue(
            Exception exception, HttpServletRequest request) {

        String name = switch (exception) {
            case MethodArgumentTypeMismatchException mismatch -> mismatch.getName();
            case MissingServletRequestParameterException missing -> missing.getParameterName();
            case MissingRequestHeaderException missing -> missing.getHeaderName();
            case MissingPathVariableException missing -> missing.getVariableName();
            case MissingMatrixVariableException missing -> missing.getVariableName();
            case MissingRequestCookieException missing -> missing.getCookieName();
            default -> "unknown";
        };
        return respond(problems.create(ApiErrorCode.INVALID_PARAMETER, request, name));
    }

    /** No route matched. Answered with the same schema as a domain-level 404, never an HTML page. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiProblem> handleNoRoute(
            NoResourceFoundException exception, HttpServletRequest request) {

        return respond(problems.create(
                ApiErrorCode.RESOURCE_NOT_FOUND, request, "endpoint", exception.getResourcePath()));
    }

    /**
     * Last resort. Logs the full stack trace server-side and returns only the trace identifier, so
     * an internal failure never leaks class names or SQL to the caller.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> handleUnexpected(
            Exception exception, HttpServletRequest request) {

        String traceId = problems.traceId(request);
        log.error("Unhandled exception on {} [traceId={}]", request.getRequestURI(), traceId, exception);
        return respond(problems.create(ApiErrorCode.INTERNAL_ERROR, request, traceId));
    }

    private ResponseEntity<ApiProblem> respond(ApiProblem problem) {
        return ResponseEntity.status(problem.status())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    /**
     * Converts a Spring property path into a JSON Pointer: {@code items[0].quantity} becomes
     * {@code /items/0/quantity}.
     */
    private static String jsonPointer(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "";
        }
        String normalized = propertyPath
                .replace("[", ".")
                .replace("]", "")
                .replace("..", ".");
        return "/" + String.join("/", normalized.split("\\."));
    }

    /** Bare constraint name from a resolvable's code list, e.g. {@code Min} out of {@code Min.age}. */
    private static String constraintOf(MessageSourceResolvable error) {
        String[] codes = error.getCodes();
        if (codes == null || codes.length == 0) {
            return "Invalid";
        }
        String last = codes[codes.length - 1];
        int dot = last.lastIndexOf('.');
        return dot < 0 ? last : last.substring(dot + 1);
    }
}
