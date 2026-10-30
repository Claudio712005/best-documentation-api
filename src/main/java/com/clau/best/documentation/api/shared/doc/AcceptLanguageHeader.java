package com.clau.best.documentation.api.shared.doc;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
