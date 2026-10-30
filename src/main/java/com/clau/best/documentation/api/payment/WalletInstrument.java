package com.clau.best.documentation.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Schema(name = "WalletInstrument", description = "doc.schema.WalletInstrument.description")
public final class WalletInstrument extends PaymentInstrument {

    @Schema(
            description = "doc.schema.WalletInstrument.provider",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private final WalletProvider provider;

    @Schema(
            description = "doc.schema.WalletInstrument.accountReference",
            examples = "ana.souza@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 120)
    private final String accountReference;

    @Schema(
            description = "doc.schema.WalletInstrument.deviceToken",
            examples = "tok_1Nv8xQ2eZvKYlo2C",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(max = 255)
    private final String deviceToken;

    @JsonCreator
    public WalletInstrument(
            @JsonProperty("label") String label,
            @JsonProperty("holderName") String holderName,
            @JsonProperty("preferred") @Nullable Boolean preferred,
            @JsonProperty("provider") WalletProvider provider,
            @JsonProperty("accountReference") String accountReference,
            @JsonProperty("deviceToken") String deviceToken) {

        super(PaymentInstrumentType.WALLET, label, holderName, preferred);
        this.provider = provider;
        this.accountReference = accountReference;
        this.deviceToken = deviceToken;
    }

    public WalletProvider getProvider() {
        return provider;
    }

    public String getAccountReference() {
        return accountReference;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    @Schema(
            name = "WalletProvider",
            description = "doc.schema.WalletProvider.description",
            examples = "APPLE_PAY",
            enumAsRef = true)
    public enum WalletProvider {
        APPLE_PAY,
        GOOGLE_PAY,
        PAYPAL
    }
}
