package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.contract.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BoletoIssuer {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final BoletoBankSlipFormatter formatter = new BoletoBankSlipFormatter();

    public BoletoResponse issue(BoletoRequest request) {
        UUID id = UUID.randomUUID();
        OffsetDateTime issuedAt = OffsetDateTime.now();
        String barcode = formatter.barcode(request.getAmount(), request.getDueDate());
        String digitableLine = formatter.digitableLine(barcode);

        return switch (request) {
            case SimpleBoletoRequest simple -> new SimpleBoletoResponse(
                    id,
                    BoletoResponse.BoletoStatus.ISSUED,
                    barcode,
                    digitableLine,
                    simple.getAmount(),
                    simple.getDueDate(),
                    issuedAt);

            case HybridBoletoRequest hybrid -> new HybridBoletoResponse(
                    id,
                    BoletoResponse.BoletoStatus.ISSUED,
                    barcode,
                    digitableLine,
                    hybrid.getAmount(),
                    hybrid.getDueDate(),
                    issuedAt,
                    qrCodeFor(id, hybrid, issuedAt),
                    Boolean.TRUE.equals(hybrid.getAllowPartialPayment()));

            case SplitBoletoRequest split -> new SplitBoletoResponse(
                    id,
                    BoletoResponse.BoletoStatus.ISSUED,
                    barcode,
                    digitableLine,
                    split.getAmount(),
                    split.getDueDate(),
                    issuedAt,
                    allocate(split),
                    split.getSettlementMode());
        };
    }

    private QrCode qrCodeFor(UUID id, HybridBoletoRequest hybrid, OffsetDateTime issuedAt) {
        String payload = "00020126580014br.gov.bcb.pix0136%s5204000053039865802BR"
                .formatted(hybrid.getPixKey());
        return new QrCode(
                payload,
                URI.create("https://api.best-documentation-api.dev/v1/boletos/%s/qrcode.png".formatted(id)),
                issuedAt.plusMinutes(hybrid.getQrCodeValidityMinutes()));
    }

    private List<SplitAllocation> allocate(SplitBoletoRequest split) {
        return split.getRecipients().stream()
                .map(recipient -> new SplitAllocation(
                        recipient.name(),
                        recipient.taxId(),
                        recipient.percentage(),
                        shareOf(split.getAmount(), recipient.percentage())))
                .toList();
    }

    private Money shareOf(Money total, BigDecimal percentage) {
        BigDecimal share = total.amount()
                .multiply(percentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_EVEN);
        return new Money(share, total.currency());
    }

    private static final class BoletoBankSlipFormatter {

        private static final String BANK_AND_CURRENCY = "3419";

        String barcode(Money amount, java.time.LocalDate dueDate) {
            long cents = amount.amount().movePointRight(2).longValueExact();
            long dueFactor = dueDate.toEpochDay() - java.time.LocalDate.of(1997, 10, 7).toEpochDay();
            long freeField = ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L);
            return "%s%04d%010d%015d".formatted(BANK_AND_CURRENCY, dueFactor, cents, freeField);
        }

        String digitableLine(String barcode) {
            return "%s.%s %s.%s %s.%s %s %s".formatted(
                    barcode.substring(0, 5), barcode.substring(5, 10),
                    barcode.substring(10, 15), barcode.substring(15, 20),
                    barcode.substring(20, 25), barcode.substring(25, 30),
                    barcode.substring(30, 31), barcode.substring(31));
        }
    }
}
