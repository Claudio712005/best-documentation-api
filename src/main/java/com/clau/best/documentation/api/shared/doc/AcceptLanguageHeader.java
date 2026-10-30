package com.clau.best.documentation.api.shared.doc;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents the {@code Accept-Language} header on an operation.
 *
 * <p>Exists as a meta-annotation because the header applies to every operation in the API. Spelling
 * the full {@code @Parameter} out at each one guarantees the descriptions drift apart, and a header
 * documented three different ways is indistinguishable from three different headers to anyone
 * reading the document.
 *
 * <p>The header is never declared as a controller method argument: Spring resolves the locale
 * through the configured {@code LocaleResolver}, so binding it would create a second, competing
 * source of truth.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(
        in = ParameterIn.HEADER,
        name = "Accept-Language",
        description = "doc.header.accept-language.description",
        required = false,
        example = "pt-BR;q=1.0, en-US;q=0.8",
        schema = @Schema(type = "string", defaultValue = "en-US"))
public @interface AcceptLanguageHeader {
}
