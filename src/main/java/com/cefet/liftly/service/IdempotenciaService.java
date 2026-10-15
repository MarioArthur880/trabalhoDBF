package com.cefet.liftly.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** The PostgreSQL transaction lock serializes retries, including across API instances. */
@Service
public class IdempotenciaService {
    @PersistenceContext private EntityManager entityManager;

    public void atualizar(Object entidade) { entityManager.refresh(entidade); }

    public String reservar(String escopo, String usuarioId, String chave) {
        if (chave == null || chave.isBlank()) return UUID.randomUUID().toString();
        if (!chave.matches("[a-zA-Z0-9-]{16,80}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chave de envio inválida");
        String recurso = escopo + ":" + usuarioId + ":" + chave;
        entityManager.createNativeQuery("select 1 from pg_advisory_xact_lock(hashtextextended(:recurso, 0))")
                .setParameter("recurso", recurso).getSingleResult();
        return UUID.nameUUIDFromBytes(recurso.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
