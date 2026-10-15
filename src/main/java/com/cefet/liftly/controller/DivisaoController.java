package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.model.Divisao;
import com.cefet.liftly.model.ExercicioTreino;
import com.cefet.liftly.repository.DivisaoRepository;
import jakarta.validation.Valid;
import com.cefet.liftly.service.IdempotenciaService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/divisoes")
@RequiredArgsConstructor
public class DivisaoController {
    private final DivisaoRepository divisaoRepository;
    private final IdempotenciaService idempotenciaService;

    @GetMapping
    public ResponseEntity<List<Divisao>> listarPorUsuario(
            @RequestParam String usuarioId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!usuarioId.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(divisaoRepository.findByUsuarioId(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Divisao> buscarPorId(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        return divisaoRepository.findById(id)
                .filter(divisao -> divisao.getUsuarioId().equals(usuarioAutenticadoId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Divisao> criar(
            @Valid @RequestBody Divisao divisao,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveEnvio,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (divisao.getUsuarioId() != null && !divisao.getUsuarioId().equals(usuarioAutenticadoId)) {
            return ResponseEntity.status(403).build();
        }
        String id = idempotenciaService.reservar("divisoes", usuarioAutenticadoId, chaveEnvio);
        var existente = divisaoRepository.findById(id);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());
        divisao.setId(id);
        divisao.setUsuarioId(usuarioAutenticadoId);
        divisao.setDataCriacao(Instant.now().toString());
        prepararIdsExercicios(divisao, null);
        return ResponseEntity.status(201).body(divisaoRepository.save(divisao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Divisao> atualizar(
            @PathVariable String id,
            @Valid @RequestBody Divisao divisao,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        var existenteOpt = divisaoRepository.findById(id);
        if (existenteOpt.isEmpty()) return ResponseEntity.notFound().build();
        Divisao existente = existenteOpt.get();
        if (!existente.getUsuarioId().equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        Set<String> idsExistentes = new HashSet<>();
        if (existente.getExercicios() != null) {
            existente.getExercicios().forEach(e -> idsExistentes.add(e.getId()));
        }

        divisao.setId(id);
        divisao.setUsuarioId(usuarioAutenticadoId);
        divisao.setDataCriacao(existente.getDataCriacao());
        prepararIdsExercicios(divisao, idsExistentes);
        return ResponseEntity.ok(divisaoRepository.save(divisao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        var divisaoOpt = divisaoRepository.findById(id);
        if (divisaoOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!divisaoOpt.get().getUsuarioId().equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        divisaoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void prepararIdsExercicios(Divisao divisao, Set<String> idsPermitidos) {
        Set<String> vistos = new HashSet<>();
        Set<String> idsUsados = new HashSet<>();
        if (divisao.getExercicios() == null) return;
        for (ExercicioTreino e : divisao.getExercicios()) {
            String chave = e.getCatalogoId() == null || e.getCatalogoId().isBlank()
                    ? e.getNome().trim().toLowerCase(java.util.Locale.ROOT) : e.getCatalogoId();
            if (!vistos.add(chave)) throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Exercício repetido na divisão");
            if (idsPermitidos == null || e.getId() == null || e.getId().isBlank() || !idsPermitidos.contains(e.getId()) || idsUsados.contains(e.getId())) {
                e.setId(UUID.randomUUID().toString());
            }
            idsUsados.add(e.getId());
            if (e.getDescansoSegundos() == null) e.setDescansoSegundos(0);
        }
    }
}
