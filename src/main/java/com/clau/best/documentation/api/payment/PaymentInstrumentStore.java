package com.clau.best.documentation.api.payment;

import com.clau.best.documentation.api.shared.error.ApiException;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PaymentInstrumentStore {

    private static final String RESOURCE = "payment instrument";

    private final Map<UUID, PaymentInstrumentResource> instruments = new ConcurrentHashMap<>();

    public PaymentInstrumentResource register(PaymentInstrument instrument) {
        PaymentInstrumentResource resource = new PaymentInstrumentResource(
                UUID.randomUUID(), OffsetDateTime.now(), instrument);
        instruments.put(resource.id(), resource);
        return resource;
    }

    public PaymentInstrumentResource require(UUID id) {
        PaymentInstrumentResource resource = instruments.get(id);
        if (resource == null) {
            throw new ApiException.ResourceNotFound(RESOURCE, id);
        }
        return resource;
    }

    public List<PaymentInstrumentResource> findAll() {
        return instruments.values().stream()
                .sorted(Comparator.comparing(PaymentInstrumentResource::registeredAt))
                .toList();
    }
}
