package com.clau.best.documentation.api.shared.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class DocumentGraph {

    private DocumentGraph() {
    }

    @SuppressWarnings("rawtypes")
    public static void forEachSchema(OpenAPI openApi, Consumer<Schema> visitor) {
        Set<Schema> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        if (openApi.getComponents() != null) {
            visitAll(openApi.getComponents().getSchemas(), visitor, visited);
            if (openApi.getComponents().getParameters() != null) {
                openApi.getComponents().getParameters().values()
                        .forEach(parameter -> visitParameter(parameter, visitor, visited));
            }
            if (openApi.getComponents().getRequestBodies() != null) {
                openApi.getComponents().getRequestBodies().values()
                        .forEach(body -> visitContent(body.getContent(), visitor, visited));
            }
            if (openApi.getComponents().getResponses() != null) {
                openApi.getComponents().getResponses().values()
                        .forEach(response -> visitResponse(response, visitor, visited));
            }
        }

        if (openApi.getPaths() != null) {
            openApi.getPaths().values().forEach(path -> visitPathItem(path, visitor, visited));
        }
        if (openApi.getWebhooks() != null) {
            openApi.getWebhooks().values().forEach(path -> visitPathItem(path, visitor, visited));
        }
    }

    public static void forEachExample(OpenAPI openApi, Consumer<Example> visitor) {
        if (openApi.getComponents() != null) {
            if (openApi.getComponents().getExamples() != null) {
                openApi.getComponents().getExamples().values().forEach(visitor);
            }
            if (openApi.getComponents().getRequestBodies() != null) {
                openApi.getComponents().getRequestBodies().values()
                        .forEach(body -> exampleContent(body.getContent(), visitor));
            }
            if (openApi.getComponents().getResponses() != null) {
                openApi.getComponents().getResponses().values()
                        .forEach(response -> exampleResponse(response, visitor));
            }
            if (openApi.getComponents().getParameters() != null) {
                openApi.getComponents().getParameters().values()
                        .forEach(parameter -> exampleParameter(parameter, visitor));
            }
        }
        if (openApi.getPaths() != null) {
            openApi.getPaths().values().forEach(path -> examplePathItem(path, visitor));
        }
        if (openApi.getWebhooks() != null) {
            openApi.getWebhooks().values().forEach(path -> examplePathItem(path, visitor));
        }
    }

    private static void examplePathItem(PathItem path, Consumer<Example> visitor) {
        if (path == null) {
            return;
        }
        if (path.getParameters() != null) {
            path.getParameters().forEach(parameter -> exampleParameter(parameter, visitor));
        }
        path.readOperations().forEach(operation -> {
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> exampleParameter(parameter, visitor));
            }
            if (operation.getRequestBody() != null) {
                exampleContent(operation.getRequestBody().getContent(), visitor);
            }
            if (operation.getResponses() != null) {
                operation.getResponses().values().forEach(response -> exampleResponse(response, visitor));
            }
        });
    }

    private static void exampleResponse(ApiResponse response, Consumer<Example> visitor) {
        if (response != null) {
            exampleContent(response.getContent(), visitor);
        }
    }

    private static void exampleParameter(Parameter parameter, Consumer<Example> visitor) {
        if (parameter == null) {
            return;
        }
        if (parameter.getExamples() != null) {
            parameter.getExamples().values().forEach(visitor);
        }
        exampleContent(parameter.getContent(), visitor);
    }

    private static void exampleContent(Content content, Consumer<Example> visitor) {
        if (content == null) {
            return;
        }
        for (MediaType mediaType : content.values()) {
            if (mediaType.getExamples() != null) {
                mediaType.getExamples().values().forEach(visitor);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void visitPathItem(PathItem path, Consumer<Schema> visitor, Set<Schema> visited) {
        if (path == null) {
            return;
        }
        if (path.getParameters() != null) {
            path.getParameters().forEach(parameter -> visitParameter(parameter, visitor, visited));
        }
        path.readOperations().forEach(operation -> visitOperation(operation, visitor, visited));
    }

    @SuppressWarnings("rawtypes")
    private static void visitOperation(
            Operation operation, Consumer<Schema> visitor, Set<Schema> visited) {

        if (operation == null) {
            return;
        }
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(parameter -> visitParameter(parameter, visitor, visited));
        }
        RequestBody body = operation.getRequestBody();
        if (body != null) {
            visitContent(body.getContent(), visitor, visited);
        }
        if (operation.getResponses() != null) {
            operation.getResponses().values()
                    .forEach(response -> visitResponse(response, visitor, visited));
        }
    }

    @SuppressWarnings("rawtypes")
    private static void visitResponse(
            ApiResponse response, Consumer<Schema> visitor, Set<Schema> visited) {

        if (response == null) {
            return;
        }
        visitContent(response.getContent(), visitor, visited);
        if (response.getHeaders() != null) {
            response.getHeaders().values()
                    .forEach(header -> visit(header.getSchema(), visitor, visited));
        }
    }

    @SuppressWarnings("rawtypes")
    private static void visitParameter(
            Parameter parameter, Consumer<Schema> visitor, Set<Schema> visited) {

        if (parameter == null) {
            return;
        }
        visit(parameter.getSchema(), visitor, visited);
        visitContent(parameter.getContent(), visitor, visited);
    }

    @SuppressWarnings("rawtypes")
    private static void visitContent(Content content, Consumer<Schema> visitor, Set<Schema> visited) {
        if (content == null) {
            return;
        }
        for (MediaType mediaType : content.values()) {
            visit(mediaType.getSchema(), visitor, visited);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void visit(Schema schema, Consumer<Schema> visitor, Set<Schema> visited) {
        if (schema == null || !visited.add(schema)) {
            return;
        }
        visitor.accept(schema);

        visitAll(schema.getProperties(), visitor, visited);
        visitAll(schema.getPatternProperties(), visitor, visited);
        visit(schema.getItems(), visitor, visited);
        visit(schema.getNot(), visitor, visited);
        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            visit(additional, visitor, visited);
        }
        visitAll(schema.getAllOf(), visitor, visited);
        visitAll(schema.getAnyOf(), visitor, visited);
        visitAll(schema.getOneOf(), visitor, visited);
    }

    @SuppressWarnings("rawtypes")
    private static void visitAll(
            Map<String, Schema> schemas, Consumer<Schema> visitor, Set<Schema> visited) {

        if (schemas != null) {

            List.copyOf(schemas.values()).forEach(schema -> visit(schema, visitor, visited));
        }
    }

    @SuppressWarnings("rawtypes")
    private static void visitAll(
            List<Schema> schemas, Consumer<Schema> visitor, Set<Schema> visited) {

        if (schemas != null) {
            List.copyOf(schemas).forEach(schema -> visit(schema, visitor, visited));
        }
    }
}
