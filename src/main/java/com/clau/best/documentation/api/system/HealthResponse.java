package com.clau.best.documentation.api.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * The smallest response in this API: three scalars, no nesting, no references.
 *
 * <p>Kept deliberately trivial as a baseline — it is the control case against which the large
 * order representation should be read.
 */
@Schema(name = "Health", description = "doc.schema.Health.description")
public record HealthResponse(

        @Schema(description = "doc.schema.Health.status", examples = "UP")
        Status status,

        @Schema(description = "doc.schema.Health.version", examples = "1.0.0")
        String version,

        @Schema(description = "doc.schema.Health.checkedAt", format = "date-time")
        OffsetDateTime checkedAt) {

    /**
     * {@code enumAsRef} publishes this enum as its own component schema.
     *
     * <p>Without it swagger inlines the enum into every property that uses it: the value list is
     * duplicated at each use site, the enum's own description is discarded, and generated clients
     * produce a separate anonymous type per occurrence instead of one shared one.
     */
    @Schema(
            name = "HealthStatus",
            description = "doc.schema.HealthStatus.description",
            enumAsRef = true)
    public enum Status {
        UP,
        DEGRADED
    }
}
