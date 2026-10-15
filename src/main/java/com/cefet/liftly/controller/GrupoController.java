package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.dto.ConvidarRequest;
import com.cefet.liftly.dto.GrupoDetalheResponse;
import com.cefet.liftly.dto.GrupoResponse;
import com.cefet.liftly.dto.MembroRankingResponse;
import com.cefet.liftly.model.Convite;
import com.cefet.liftly.model.Grupo;
import com.cefet.liftly.model.Historico;
import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.ConviteRepository;
import com.cefet.liftly.repository.GrupoRepository;
import com.cefet.liftly.repository.HistoricoRepository;
import com.cefet.liftly.repository.UsuarioRepository;
import jakarta.validation.Valid;
import com.cefet.liftly.service.IdempotenciaService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {
    private final GrupoRepository grupoRepository;
    private final IdempotenciaService idempotenciaService;
    private final UsuarioRepository usuarioRepository;
    private final ConviteRepository conviteRepository;
    private final HistoricoRepository historicoRepository;

    @GetMapping
    public ResponseEntity<List<GrupoResponse>> listarPorUsuario(
            @RequestParam String usuarioId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!usuarioId.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(grupoRepository.findByMembro(usuarioId).stream().map(this::paraResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDetalheResponse> buscarDetalhe(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        return grupoRepository.findById(id)
                .filter(grupo -> grupo.getMembrosIds().contains(usuarioAutenticadoId))
                .map(grupo -> ResponseEntity.ok(montarDetalhe(grupo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<GrupoResponse> criar(
            @Valid @RequestBody Grupo grupo,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveEnvio,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (grupo.getCriadorId() != null && !grupo.getCriadorId().equals(usuarioAutenticadoId)) {
            return ResponseEntity.status(403).build();
        }
        String id = idempotenciaService.reservar("grupos", usuarioAutenticadoId, chaveEnvio);
        var existente = grupoRepository.findById(id);
        if (existente.isPresent()) return ResponseEntity.ok(paraResponse(existente.get()));
        grupo.setId(id);
        grupo.setCriadorId(usuarioAutenticadoId);
        grupo.setCodigoConvite(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        grupo.setDataCriacao(Instant.now().toString());
        grupo.setMembrosIds(new ArrayList<>(List.of(usuarioAutenticadoId)));
        return ResponseEntity.status(201).body(paraResponse(grupoRepository.save(grupo)));
    }

    @PostMapping("/entrar")
    @Transactional
    public ResponseEntity<GrupoResponse> entrarPorCodigo(
            @RequestParam String codigo,
            @RequestParam(required = false) String usuarioId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (usuarioId != null && !usuarioId.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();
        return grupoRepository.findByCodigoConvite(codigo.trim().toUpperCase(Locale.ROOT))
                .map(grupo -> {
                    idempotenciaService.reservar("membros", grupo.getId(), "membership-guard-0000");
                    idempotenciaService.atualizar(grupo);
                    grupo.setMembrosIds(new ArrayList<>(new LinkedHashSet<>(grupo.getMembrosIds())));
                    if (!grupo.getMembrosIds().contains(usuarioAutenticadoId)) {
                        grupo.getMembrosIds().add(usuarioAutenticadoId);
                        grupoRepository.save(grupo);
                    }
                    return ResponseEntity.ok(paraResponse(grupo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/convidar")
    @Transactional
    public ResponseEntity<Void> convidar(
            @PathVariable String id,
            @Valid @RequestBody ConvidarRequest request,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!request.deUsuarioId().equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        idempotenciaService.reservar("convites", id, "invitation-guard-0000");
        Optional<Grupo> grupoOpt = grupoRepository.findById(id);
        Optional<Usuario> convidadoOpt = usuarioRepository.findByEmailIgnoreCase(request.email().trim());
        Optional<Usuario> convidanteOpt = usuarioRepository.findById(usuarioAutenticadoId);

        if (grupoOpt.isEmpty() || convidadoOpt.isEmpty() || convidanteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Grupo grupo = grupoOpt.get();
        if (!grupo.getMembrosIds().contains(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        Usuario convidado = convidadoOpt.get();
        if (grupo.getMembrosIds().contains(convidado.getId())
                || conviteRepository.existsByGrupoIdAndParaUsuarioIdAndStatus(id, convidado.getId(), "PENDENTE")) {
            return ResponseEntity.status(409).build();
        }

        Convite convite = new Convite();
        convite.setId(UUID.randomUUID().toString());
        convite.setGrupoId(grupo.getId());
        convite.setGrupoNome(grupo.getNome());
        convite.setDeUsuarioId(usuarioAutenticadoId);
        convite.setDeUsuarioNome(convidanteOpt.get().getNome());
        convite.setParaUsuarioId(convidado.getId());
        convite.setStatus("PENDENTE");
        convite.setDataEnvio(Instant.now().toString());
        conviteRepository.save(convite);

        return ResponseEntity.status(201).build();
    }

    private GrupoResponse paraResponse(Grupo grupo) {
        return new GrupoResponse(grupo.getId(), grupo.getNome(), grupo.getCodigoConvite(),
                grupo.getCriadorId(), new HashSet<>(grupo.getMembrosIds()).size());
    }

    private GrupoDetalheResponse montarDetalhe(Grupo grupo) {
        LocalDate hoje = LocalDate.now(ZoneOffset.UTC);
        LocalDate inicioSemana = hoje.minusDays(6);

        List<MembroRankingResponse> ranking = grupo.getMembrosIds().stream().distinct()
                .map(usuarioId -> {
                    String nome = usuarioRepository.findById(usuarioId).map(Usuario::getNome).orElse("Usuário");
                    List<Historico> historicos = historicoRepository.findByUsuarioIdOrderByDataInicioDesc(usuarioId);
                    Set<LocalDate> diasTreinados = historicos.stream()
                            .map(h -> converterData(h.getDataInicio()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    int treinosNaSemana = (int) diasTreinados.stream()
                            .filter(d -> !d.isBefore(inicioSemana) && !d.isAfter(hoje)).count();
                    int streak = calcularStreak(diasTreinados, hoje);
                    return new MembroRankingResponse(usuarioId, nome, streak, treinosNaSemana);
                })
                .sorted(Comparator.comparingInt(MembroRankingResponse::streakAtual).reversed())
                .toList();

        return new GrupoDetalheResponse(grupo.getId(), grupo.getNome(), grupo.getCodigoConvite(), ranking);
    }

    private LocalDate converterData(String data) {
        if (data == null || data.isBlank()) return null;
        try {
            return Instant.parse(data).atZone(ZoneOffset.UTC).toLocalDate();
        } catch (Exception e) {
            try {
                return LocalDate.parse(data.substring(0, 10));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private int calcularStreak(Set<LocalDate> dias, LocalDate hoje) {
        int streak = 0;
        LocalDate atual = dias.contains(hoje) ? hoje : hoje.minusDays(1);
        while (dias.contains(atual)) {
            streak++;
            atual = atual.minusDays(1);
        }
        return streak;
    }
}
