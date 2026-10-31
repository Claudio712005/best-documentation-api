package com.clau.best.documentation.api.shared.doc;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

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
        name = "X-Request-Id",
        description = "doc.header.request-id.description",
        required = false,
        example = "01JC8Z5W9QK3MV2T7X4YB6NDHA",
        schema = @Schema(type = "string", maxLength = 64))
public @interface RequestIdHeader {
}
