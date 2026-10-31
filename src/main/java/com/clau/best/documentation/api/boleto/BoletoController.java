package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.config.OpenApiConfig;
import com.clau.best.documentation.api.shared.doc.AcceptLanguageHeader;
import com.clau.best.documentation.api.shared.doc.NotFoundResponse;
import com.clau.best.documentation.api.shared.doc.RequestIdHeader;
import com.clau.best.documentation.api.shared.doc.StandardErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/boletos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = OpenApiConfig.TAG_BOLETOS)
@AcceptLanguageHeader
@RequestIdHeader
@StandardErrorResponses
public class BoletoController {

    private final BoletoIssuer issuer;
    private final BoletoStore store;

    public BoletoController(BoletoIssuer issuer, BoletoStore store) {
        this.issuer = issuer;
        this.store = store;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "issueBoleto",
            summary = "doc.operation.boleto.issue.summary",
            description = "doc.operation.boleto.issue.description",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "doc.request.boleto.issue",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "SIMPLE",
                                            summary = "doc.example.boleto.request.simple",
                                            description = "doc.example.boleto.request.simple.description",
                                            value = """
                                                    {
                                                      "category": "SIMPLE",
                                                      "payer": {
                                                        "name": "Ana Paula Souza",
                                                        "taxId": { "value": "12345678901" },
                                                        "postalCode": "01310-100"
                                                      },
                                                      "amount": { "amount": 149.90, "currency": "BRL" },
                                                      "dueDate": "2026-12-05"
                                                    }"""),
                                    @ExampleObject(
                                            name = "HYBRID",
                                            summary = "doc.example.boleto.request.hybrid",
                                            description = "doc.example.boleto.request.hybrid.description",
                                            value = """
                                                    {
                                                      "category": "HYBRID",
                                                      "payer": {
                                                        "name": "Ana Paula Souza",
                                                        "taxId": { "value": "12345678901" },
                                                        "postalCode": "01310-100"
                                                      },
                                                      "amount": { "amount": 149.90, "currency": "BRL" },
                                                      "dueDate": "2026-12-05",
                                                      "reference": "INV-2026-0045",
                                                      "pixKey": "cobranca@example.com",
                                                      "qrCodeValidityMinutes": 60,
                                                      "allowPartialPayment": false
                                                    }"""),
                                    @ExampleObject(
                                            name = "SPLIT",
                                            summary = "doc.example.boleto.request.split",
                                            description = "doc.example.boleto.request.split.description",
                                            value = """
                                                    {
                                                      "category": "SPLIT",
                                                      "payer": {
                                                        "name": "Ana Paula Souza",
                                                        "taxId": { "value": "12345678901" },
                                                        "postalCode": "01310-100"
                                                      },
                                                      "amount": { "amount": 1000.00, "currency": "BRL" },
                                                      "dueDate": "2026-12-05",
                                                      "settlementMode": "ON_PAYMENT",
                                                      "statementDescriptor": "MARKETPLACE",
                                                      "recipients": [
                                                        {
                                                          "name": "Marketplace Seller Ltda",
                                                          "taxId": { "value": "12345678000195" },
                                                          "percentage": 70.00
                                                        },
                                                        {
                                                          "name": "Platform Fee",
                                                          "taxId": { "value": "98765432000110" },
                                                          "percentage": 30.00
                                                        }
                                                      ]
                                                    }""")
                            })))
    @ApiResponse(
            responseCode = "201",
            description = "doc.response.boleto.issue.201",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BoletoResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "SIMPLE",
                                    summary = "doc.example.boleto.response.simple",
                                    description = "doc.example.boleto.response.simple.description",
                                    value = """
                                            {
                                              "id": "8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27",
                                              "category": "SIMPLE",
                                              "status": "ISSUED",
                                              "barcode": "341910710000014990402817453900001234",
                                              "digitableLine": "34191.07100 00014.990402 81745.390000 1 234",
                                              "amount": { "amount": 149.90, "currency": "BRL" },
                                              "dueDate": "2026-12-05",
                                              "issuedAt": "2026-10-30T21:14:05.182-03:00"
                                            }"""),
                            @ExampleObject(
                                    name = "HYBRID",
                                    summary = "doc.example.boleto.response.hybrid",
                                    description = "doc.example.boleto.response.hybrid.description",
                                    value = """
                                            {
                                              "id": "8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27",
                                              "category": "HYBRID",
                                              "status": "ISSUED",
                                              "barcode": "341910710000014990402817453900001234",
                                              "digitableLine": "34191.07100 00014.990402 81745.390000 1 234",
                                              "amount": { "amount": 149.90, "currency": "BRL" },
                                              "dueDate": "2026-12-05",
                                              "issuedAt": "2026-10-30T21:14:05.182-03:00",
                                              "qrCode": {
                                                "payload": "00020126580014br.gov.bcb.pix0136cobranca@example.com5204000053039865802BR",
                                                "imageUri": "https://api.best-documentation-api.dev/v1/boletos/8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27/qrcode.png",
                                                "expiresAt": "2026-10-30T22:14:05.182-03:00"
                                              },
                                              "partialPaymentAllowed": false
                                            }"""),
                            @ExampleObject(
                                    name = "SPLIT",
                                    summary = "doc.example.boleto.response.split",
                                    description = "doc.example.boleto.response.split.description",
                                    value = """
                                            {
                                              "id": "8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27",
                                              "category": "SPLIT",
                                              "status": "ISSUED",
                                              "barcode": "341910710000100000402817453900001234",
                                              "digitableLine": "34191.07100 00100.000402 81745.390000 1 234",
                                              "amount": { "amount": 1000.00, "currency": "BRL" },
                                              "dueDate": "2026-12-05",
                                              "issuedAt": "2026-10-30T21:14:05.182-03:00",
                                              "settlementMode": "ON_PAYMENT",
                                              "splits": [
                                                {
                                                  "name": "Marketplace Seller Ltda",
                                                  "taxId": { "value": "12345678000195", "kind": "COMPANY" },
                                                  "percentage": 70.00,
                                                  "amount": { "amount": 700.00, "currency": "BRL" }
                                                },
                                                {
                                                  "name": "Platform Fee",
                                                  "taxId": { "value": "98765432000110", "kind": "COMPANY" },
                                                  "percentage": 30.00,
                                                  "amount": { "amount": 300.00, "currency": "BRL" }
                                                }
                                              ]
                                            }""")
                    }))
    public ResponseEntity<BoletoResponse> issue(@Valid @RequestBody BoletoRequest request) {
        BoletoResponse boleto = store.save(issuer.issue(request));
        return ResponseEntity
                .created(UriComponentsBuilder.fromPath("/v1/boletos/{id}")
                        .buildAndExpand(boleto.getId())
                        .toUri())
                .body(boleto);
    }

    @GetMapping("/{boletoId}")
    @Operation(
            operationId = "getBoleto",
            summary = "doc.operation.boleto.get.summary",
            description = "doc.operation.boleto.get.description")
    @ApiResponse(responseCode = "200", description = "doc.response.boleto.get.200")
    @NotFoundResponse
    public BoletoResponse get(
            @Parameter(
                    description = "doc.parameter.boleto.boletoId",
                    required = true,
                    example = "8c5f2d41-7b3e-4a19-9d6c-1e0f5a8b3c27")
            @PathVariable UUID boletoId) {

        return store.require(boletoId);
    }

    @GetMapping
    @Operation(
            operationId = "listBoletos",
            summary = "doc.operation.boleto.list.summary",
            description = "doc.operation.boleto.list.description")
    @ApiResponse(responseCode = "200", description = "doc.response.boleto.list.200")
    public List<BoletoResponse> list() {
        return store.findAll();
    }
}
