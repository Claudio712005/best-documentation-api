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

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    public static final String JSON_SCHEMA_DIALECT = "https://spec.openapis.org/oas/3.1/dialect/base";

    public static final String TAG_SYSTEM = "system";
    public static final String TAG_CATALOG = "catalog";
    public static final String TAG_CARTS = "carts";
    public static final String TAG_PAYMENT_INSTRUMENTS = "payment-instruments";
    public static final String TAG_BOLETOS = "boletos";
    public static final String TAG_ORDERS = "orders";

    public static final List<String> TAG_ORDER = List.of(
            TAG_SYSTEM, TAG_PAYMENT_INSTRUMENTS, TAG_BOLETOS);

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

    private List<Server> servers(ApiProperties api) {
        return api.servers().stream()
                .map(server -> new Server().url(server.url()).description(server.description()))
                .toList();
    }
}
