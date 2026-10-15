package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.model.Historico;
import com.cefet.liftly.model.HistoricoExercicio;
import com.cefet.liftly.repository.DivisaoRepository;
import com.cefet.liftly.repository.HistoricoRepository;
import jakarta.validation.Valid;
import com.cefet.liftly.service.IdempotenciaService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/historicos")
@RequiredArgsConstructor
public class HistoricoController {
    private final HistoricoRepository historicoRepository;
    private final IdempotenciaService idempotenciaService;
    private final DivisaoRepository divisaoRepository;

    @GetMapping
    public ResponseEntity<List<Historico>> listarPorUsuario(
            @RequestParam String usuarioId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!usuarioId.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(historicoRepository.findByUsuarioIdOrderByDataInicioDesc(usuarioId));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Historico> salvar(
            @Valid @RequestBody Historico historico,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveEnvio,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (historico.getUsuarioId() != null && !historico.getUsuarioId().equals(usuarioAutenticadoId)) {
            return ResponseEntity.status(403).build();
        }
        if (!datasValidas(historico)) return ResponseEntity.badRequest().build();

        if (historico.getDivisaoId() != null && !historico.getDivisaoId().isBlank()) {
            var divisaoOpt = divisaoRepository.findById(historico.getDivisaoId());
            if (divisaoOpt.isPresent()) {
                if (!divisaoOpt.get().getUsuarioId().equals(usuarioAutenticadoId)) {
                    return ResponseEntity.status(403).build();
                }
                historico.setDivisaoNome(divisaoOpt.get().getNome());
            }
        }

        String id = idempotenciaService.reservar("historicos", usuarioAutenticadoId, chaveEnvio);
        var existente = historicoRepository.findById(id);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());
        historico.setId(id);
        historico.setUsuarioId(usuarioAutenticadoId);
        if (historico.getExercicios() != null) {
            for (HistoricoExercicio e : historico.getExercicios()) {
                e.setId(UUID.randomUUID().toString());
                if (e.getAvulso() == null) e.setAvulso(false);
                if (e.getDescansoSegundos() == null) e.setDescansoSegundos(0);
                if (e.getSeriesFeitas() == null || e.getSeriesFeitas().size() != e.getSeries()) {
                    return ResponseEntity.badRequest().build();
                }
            }
        }
        return ResponseEntity.status(201).body(historicoRepository.save(historico));
    }

    private boolean datasValidas(Historico historico) {
        try {
            Instant inicio = Instant.parse(historico.getDataInicio());
            Instant fim = Instant.parse(historico.getDataFim());
            return !fim.isBefore(inicio);
        } catch (Exception e) {
            return false;
        }
    }
}
