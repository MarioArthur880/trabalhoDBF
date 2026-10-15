package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.dto.*;
import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.UsuarioRepository;
import com.cefet.liftly.service.AutenticacaoService;
import com.cefet.liftly.service.PasswordService;
import com.cefet.liftly.service.PesoService;
import org.springframework.security.core.AuthenticationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    private final AutenticacaoService autenticacaoService;
    private final PesoService pesoService;
    private final com.cefet.liftly.service.IdempotenciaService idempotenciaService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(autenticacaoService.autenticar(request));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/cadastro")
    @Transactional
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        String email = normalizarEmail(request.email());
        idempotenciaService.reservar("cadastro", email, "registration-guard-0000");
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(409).build();
        }

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID().toString());
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordService.hash(request.senha()));
        usuario.setAtivo(true);
        return ResponseEntity.status(201).body(UsuarioResponse.from(usuarioRepository.save(usuario)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String header) {
        // JWT stateless: a aplicação cliente descarta o token.
        // A assinatura continua válida até a expiração (padrão: 24 h).
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/{id}")
    @Transactional
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId,
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        if (!id.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        return usuarioRepository.findById(id).map(usuario -> {
            pesoService.registrarAlteracao(usuario, request.peso());
            usuario.setNome(request.nome().trim());
            usuario.setDataNascimento(limpar(request.dataNascimento()));
            usuario.setPeso(request.peso());
            usuario.setAltura(request.altura());
            usuario.setObjetivo(limpar(request.objetivo()));
            return ResponseEntity.ok(UsuarioResponse.from(usuarioRepository.save(usuario)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/usuarios/{id}/senha")
    public ResponseEntity<Void> alterarSenha(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId,
            @Valid @RequestBody AlterarSenhaRequest request) {
        if (!id.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        return usuarioRepository.findById(id).map(usuario -> {
            if (!passwordService.confere(request.senhaAtual(), usuario.getSenha())) {
                return ResponseEntity.status(401).<Void>build();
            }
            usuario.setSenha(passwordService.hash(request.novaSenha()));
            usuarioRepository.save(usuario);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable String id,
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        if (!id.equals(usuarioAutenticadoId)) return ResponseEntity.status(403).build();

        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setAtivo(false);
            usuarioRepository.save(usuario);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String limpar(String valor) {
        if (valor == null) return null;
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
