package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.dto.MensagemRequest;
import com.cefet.liftly.dto.MensagemResponse;
import com.cefet.liftly.model.MensagemGrupo;
import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.GrupoRepository;
import com.cefet.liftly.repository.MensagemGrupoRepository;
import com.cefet.liftly.repository.UsuarioRepository;
import jakarta.validation.Valid;
import com.cefet.liftly.service.IdempotenciaService;
import com.cefet.liftly.service.ImagemChatService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos/{grupoId}/mensagens")
@RequiredArgsConstructor
public class MensagemGrupoController {
    private final GrupoRepository grupoRepository;
    private final MensagemGrupoRepository mensagemGrupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdempotenciaService idempotenciaService;
    private final ImagemChatService imagemChatService;

    @GetMapping
    public ResponseEntity<List<MensagemResponse>> listar(
            @PathVariable String grupoId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!ehMembro(grupoId, usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        List<MensagemGrupo> mensagens = new ArrayList<>(mensagemGrupoRepository.findTop100ByGrupoIdOrderByDataEnvioDesc(grupoId));
        Collections.reverse(mensagens);

        Set<String> autores = mensagens.stream().map(MensagemGrupo::getAutorId).collect(Collectors.toSet());
        Map<String, Usuario> usuarios = usuarioRepository.findAllById(autores).stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        List<MensagemResponse> resposta = mensagens.stream().map(m -> {
            Usuario autor = usuarios.get(m.getAutorId());
            String nome = autor != null ? autor.getNome() : "Usuário";
            return new MensagemResponse(m.getId(), m.getGrupoId(), m.getAutorId(), nome, m.getTexto(), m.getImagem(), m.getDataEnvio());
        }).toList();
        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<MensagemResponse> enviar(
            @PathVariable String grupoId,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveEnvio,
            @Valid @RequestBody MensagemRequest request) {
        if (!ehMembro(grupoId, usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        String texto = sanitizar(request.texto());
        String imagem = imagemChatService.validar(request.imagem());
        if (texto.isBlank() && imagem == null) return ResponseEntity.badRequest().build();

        String id = idempotenciaService.reservar("mensagens:" + grupoId, usuarioAutenticadoId, chaveEnvio);
        var existente = mensagemGrupoRepository.findById(id);
        if (existente.isPresent()) {
            MensagemGrupo m = existente.get();
            String nome = usuarioRepository.findById(usuarioAutenticadoId).map(Usuario::getNome).orElse("Usuário");
            return ResponseEntity.ok(new MensagemResponse(m.getId(), grupoId, usuarioAutenticadoId,
                    nome, m.getTexto(), m.getImagem(), m.getDataEnvio()));
        }
        MensagemGrupo mensagem = new MensagemGrupo();
        mensagem.setId(id);
        mensagem.setImagem(imagem);
        mensagem.setGrupoId(grupoId);
        mensagem.setAutorId(usuarioAutenticadoId);
        mensagem.setTexto(texto);
        mensagem.setDataEnvio(Instant.now());
        mensagem = mensagemGrupoRepository.save(mensagem);

        String nome = usuarioRepository.findById(usuarioAutenticadoId).map(Usuario::getNome).orElse("Usuário");
        return ResponseEntity.status(201).body(new MensagemResponse(
                mensagem.getId(), grupoId, usuarioAutenticadoId, nome, mensagem.getTexto(), mensagem.getImagem(), mensagem.getDataEnvio()
        ));
    }

    private boolean ehMembro(String grupoId, String usuarioId) {
        return grupoRepository.findById(grupoId)
                .map(grupo -> grupo.getMembrosIds().contains(usuarioId))
                .orElse(false);
    }

    private String sanitizar(String texto) {
        if (texto == null) return "";
        String semControles = texto.replaceAll("[\\p{Cc}&&[^\\n\\t]]", "");
        return semControles.replace("<", "").replace(">", "").trim();
    }
}
