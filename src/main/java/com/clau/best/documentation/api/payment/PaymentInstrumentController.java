package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.config.OpenApiConfig;
import com.clau.best.documentation.api.shared.doc.AcceptLanguageHeader;
import com.clau.best.documentation.api.shared.doc.NotFoundResponse;
import com.clau.best.documentation.api.shared.doc.RequestIdHeader;
import com.clau.best.documentation.api.shared.doc.StandardErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping(path = "/v1/payment-instruments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = OpenApiConfig.TAG_PAYMENT_INSTRUMENTS)
@AcceptLanguageHeader
@RequestIdHeader
@StandardErrorResponses
public class PaymentInstrumentController {

    private final PaymentInstrumentStore store;

    public PaymentInstrumentController(PaymentInstrumentStore store) {
        this.store = store;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "registerPaymentInstrument",
            summary = "doc.operation.payment.register.summary",
            description = "doc.operation.payment.register.description",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "doc.request.payment.register",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PaymentInstrument.class),
                            examples = {
                                    @ExampleObject(
                                            name = "card",
                                            summary = "doc.example.payment.card",
                                            value = """
                                                    {
                                                      "type": "CARD",
                                                      "label": "Personal card",
                                                      "holderName": "ANA P SOUZA",
                                                      "preferred": true,
                                                      "number": "4111111111111111",
                                                      "expiryMonth": 9,
                                                      "expiryYear": 2031,
                                                      "securityCode": "123"
                                                    }"""),
                                    @ExampleObject(
                                            name = "pix",
                                            summary = "doc.example.payment.pix",
                                            value = """
                                                    {
                                                      "type": "PIX",
                                                      "label": "Pix e-mail",
                                                      "holderName": "ANA P SOUZA",
                                                      "keyType": "EMAIL",
                                                      "key": "ana.souza@example.com"
                                                    }"""),
                                    @ExampleObject(
                                            name = "boleto",
                                            summary = "doc.example.payment.boleto",
                                            value = """
                                                    {
                                                      "type": "BOLETO",
                                                      "label": "Bank slip",
                                                      "holderName": "ANA P SOUZA",
                                                      "taxId": "12345678901",
                                                      "postalCode": "01310-100"
                                                    }"""),
                                    @ExampleObject(
                                            name = "wallet",
                                            summary = "doc.example.payment.wallet",
                                            value = """
                                                    {
                                                      "type": "WALLET",
                                                      "label": "Phone wallet",
                                                      "holderName": "ANA P SOUZA",
                                                      "provider": "APPLE_PAY",
                                                      "accountReference": "ana.souza@example.com",
                                                      "deviceToken": "tok_1Nv8xQ2eZvKYlo2C"
                                                    }""")
                            })))
    @ApiResponse(
            responseCode = "201",
            description = "doc.response.payment.register.201",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaymentInstrumentResource.class)))
    public ResponseEntity<PaymentInstrumentResource> register(
            @Valid @RequestBody PaymentInstrument instrument) {

        PaymentInstrumentResource registered = store.register(instrument);

        return ResponseEntity
                .created(UriComponentsBuilder.fromPath("/v1/payment-instruments/{id}")
                        .buildAndExpand(registered.id())
                        .toUri())
                .body(registered);
    }

    @GetMapping("/{instrumentId}")
    @Operation(
            operationId = "getPaymentInstrument",
            summary = "doc.operation.payment.get.summary",
            description = "doc.operation.payment.get.description")
    @ApiResponse(
            responseCode = "200",
            description = "doc.response.payment.get.200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaymentInstrumentResource.class)))
    @NotFoundResponse
    public PaymentInstrumentResource get(
            @Parameter(
                    description = "doc.parameter.payment.instrumentId",
                    required = true,
                    example = "3f1a7c9e-5b2d-4e81-9a6f-0c7d8e2b4a15")
            @PathVariable UUID instrumentId) {

        return store.require(instrumentId);
    }

    @GetMapping
    @Operation(
            operationId = "listPaymentInstruments",
            summary = "doc.operation.payment.list.summary",
            description = "doc.operation.payment.list.description")
    @ApiResponse(
            responseCode = "200",
            description = "doc.response.payment.list.200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = PaymentInstrumentResource.class))))
    public List<PaymentInstrumentResource> list() {
        return store.findAll();
    }
}
