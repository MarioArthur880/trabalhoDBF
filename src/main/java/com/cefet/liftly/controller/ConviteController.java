package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.dto.ConviteResponse;
import com.cefet.liftly.repository.ConviteRepository;
import com.cefet.liftly.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import com.cefet.liftly.service.IdempotenciaService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConviteController {
    private final ConviteRepository conviteRepository;
    private final GrupoRepository grupoRepository;
    private final IdempotenciaService idempotenciaService;

    @GetMapping
    public ResponseEntity<List<ConviteResponse>> listarPendentes(
            @RequestParam String usuarioId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!usuarioId.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(conviteRepository.findByParaUsuarioIdAndStatusOrderByDataEnvioDesc(usuarioId, "PENDENTE")
                .stream()
                .map(c -> new ConviteResponse(c.getId(), c.getGrupoId(), c.getGrupoNome(), c.getDeUsuarioNome(), c.getDataEnvio()))
                .toList());
    }

    @PutMapping("/{id}/aceitar")
    @Transactional
    public ResponseEntity<Void> aceitar(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        return conviteRepository.findById(id).map(convite -> {
            if (!convite.getParaUsuarioId().equals(usuarioAutenticadoId)) return ResponseEntity.status(403).<Void>build();
            if (!"PENDENTE".equals(convite.getStatus())) return ResponseEntity.status(409).<Void>build();

            idempotenciaService.reservar("membros", convite.getGrupoId(), "membership-guard-0000");
            var grupoOpt = grupoRepository.findById(convite.getGrupoId());
            if (grupoOpt.isEmpty()) return ResponseEntity.notFound().<Void>build();
            var grupo = grupoOpt.get();
            idempotenciaService.atualizar(grupo);
            grupo.setMembrosIds(new java.util.ArrayList<>(new java.util.LinkedHashSet<>(grupo.getMembrosIds())));
            if (!grupo.getMembrosIds().contains(usuarioAutenticadoId)) {
                grupo.getMembrosIds().add(usuarioAutenticadoId);
                grupoRepository.save(grupo);
            }
            convite.setStatus("ACEITO");
            conviteRepository.save(convite);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        return conviteRepository.findById(id).map(convite -> {
            if (!convite.getParaUsuarioId().equals(usuarioAutenticadoId)) return ResponseEntity.status(403).<Void>build();
            if (!"PENDENTE".equals(convite.getStatus())) return ResponseEntity.status(409).<Void>build();
            convite.setStatus("RECUSADO");
            conviteRepository.save(convite);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
