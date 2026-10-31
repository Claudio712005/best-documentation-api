package com.clau.best.documentation.api.boleto;

import com.clau.best.documentation.api.shared.error.ApiException;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BoletoStore {

    private static final String RESOURCE = "boleto";

    private final Map<UUID, BoletoResponse> boletos = new ConcurrentHashMap<>();

    public BoletoResponse save(BoletoResponse boleto) {
        boletos.put(boleto.getId(), boleto);
        return boleto;
    }

    public BoletoResponse require(UUID id) {
        BoletoResponse boleto = boletos.get(id);
        if (boleto == null) {
            throw new ApiException.ResourceNotFound(RESOURCE, id);
        }
        return boleto;
    }

    public List<BoletoResponse> findAll() {
        return boletos.values().stream()
                .sorted(Comparator.comparing(BoletoResponse::getIssuedAt))
                .toList();
    }
}
