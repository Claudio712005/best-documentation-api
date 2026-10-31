package com.clau.best.documentation.api.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "Health", description = "doc.schema.Health.description")
public record HealthResponse(

        @Schema(description = "doc.schema.Health.status", examples = "UP")
        Status status,

        @Schema(description = "doc.schema.Health.version", examples = "1.0.0")
        String version,

        @Schema(description = "doc.schema.Health.checkedAt", format = "date-time")
        OffsetDateTime checkedAt) {

    @Schema(
            name = "HealthStatus",
            description = "doc.schema.HealthStatus.description",
            enumAsRef = true)
    public enum Status {
        UP,
        DEGRADED
    }
}
