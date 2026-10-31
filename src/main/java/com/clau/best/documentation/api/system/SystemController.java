package com.clau.best.documentation.api.system;

import com.clau.best.documentation.api.config.ApiProperties;
import com.clau.best.documentation.api.config.OpenApiConfig;
import com.clau.best.documentation.api.shared.doc.AcceptLanguageHeader;
import com.clau.best.documentation.api.shared.doc.RequestIdHeader;
import com.clau.best.documentation.api.shared.doc.StandardErrorResponses;
import com.clau.best.documentation.api.shared.i18n.SupportedLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = OpenApiConfig.TAG_SYSTEM)
@AcceptLanguageHeader
@RequestIdHeader
@StandardErrorResponses
public class SystemController {

    private final ApiProperties api;

    public SystemController(ApiProperties api) {
        this.api = api;
    }

    @GetMapping("/health")
    @Operation(
            operationId = "getHealth",
            summary = "doc.operation.system.health.summary",
            description = "doc.operation.system.health.description")
    @ApiResponse(
            responseCode = "200",
            description = "doc.response.system.health.200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HealthResponse.class)))
    public HealthResponse health() {
        return new HealthResponse(HealthResponse.Status.UP, api.version(), OffsetDateTime.now());
    }

    @GetMapping("/languages")
    @Operation(
            operationId = "listSupportedLanguages",
            summary = "doc.operation.system.languages.summary",
            description = "doc.operation.system.languages.description")
    @ApiResponse(
            responseCode = "200",
            description = "doc.response.system.languages.200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = LanguageDescriptor.class))))
    public List<LanguageDescriptor> listSupportedLanguages() {
        Locale requested = LocaleContextHolder.getLocale();
        SupportedLanguage negotiated = SupportedLanguage.closestTo(requested);
        return List.of(SupportedLanguage.values()).stream()
                .map(language -> new LanguageDescriptor(
                        language.tag(),
                        language.locale().getDisplayName(requested),
                        language == negotiated,
                        language == SupportedLanguage.DEFAULT))
                .toList();
    }
}
