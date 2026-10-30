package com.clau.best.documentation.api.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Builds the document-level structure of the OpenAPI definition.
 *
 * <p>Only structure lives here. Every piece of prose is a message key, resolved per request by
 * {@code DocumentLocalizationCustomizer}, because this bean is a singleton: text written into it
 * directly would be fixed at startup in one language.
 *
 * <p>Tags are declared explicitly and exhaustively, with {@code springdoc.auto-tag-classes=false}.
 * Letting springdoc derive them from class names publishes {@code order-controller} as a public
 * grouping, which leaks internal structure and cannot be translated.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /** JSON Schema dialect of OpenAPI 3.1. springdoc does not emit it on its own. */
    public static final String JSON_SCHEMA_DIALECT = "https://spec.openapis.org/oas/3.1/dialect/base";

    public static final String TAG_SYSTEM = "system";
    public static final String TAG_CATALOG = "catalog";
    public static final String TAG_CARTS = "carts";
    public static final String TAG_PAYMENT_INSTRUMENTS = "payment-instruments";
    public static final String TAG_ORDERS = "orders";

    /** Declaration order of the tags, which is the order the documentation UI renders them in. */
    public static final List<String> TAG_ORDER = List.of(
            TAG_SYSTEM, TAG_CATALOG, TAG_CARTS, TAG_PAYMENT_INSTRUMENTS, TAG_ORDERS);

    @Bean
    public OpenAPI openApi(ApiProperties api) {
        return new OpenAPI()
                .jsonSchemaDialect(JSON_SCHEMA_DIALECT)
                .info(info(api))
                .servers(servers(api))
                .externalDocs(new ExternalDocumentation()
                        .url(api.documentationUri().toString())
                        .description("Guides, changelog and error reference"))
                .tags(TAG_ORDER.stream().map(name -> new Tag().name(name)).toList());
    }

    private Info info(ApiProperties api) {
        return new Info()
                .title(api.title())
                .version(api.version())
                .contact(new Contact()
                        .name(api.contact().name())
                        .email(api.contact().email())
                        .url(api.contact().url().toString()))
                .license(new License()
                        .name(api.license().name())
                        .url(api.license().url().toString()));
    }

    /**
     * Declared servers, in the order clients should prefer them.
     *
     * <p>Taken from configuration rather than a {@code @Server} annotation so that each deployment
     * publishes a document naming the environment it is actually running in.
     */
    private List<Server> servers(ApiProperties api) {
        return api.servers().stream()
                .map(server -> new Server().url(server.url()).description(server.description()))
                .toList();
    }
}
