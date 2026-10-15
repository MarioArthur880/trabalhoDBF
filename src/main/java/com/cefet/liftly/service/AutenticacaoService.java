package com.cefet.liftly.service;

import com.cefet.liftly.dto.AuthResponse;
import com.cefet.liftly.dto.LoginRequest;
import com.cefet.liftly.dto.UsuarioResponse;
import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.UsuarioRepository;
import com.cefet.liftly.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Login separado do controller, no padrão de AutenticacaoService do professor. */
@Service
public class AutenticacaoService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final PasswordService senhas;
    private final JwtService jwtService;

    public AutenticacaoService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
                               PasswordService senhas, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse autenticar(LoginRequest request) throws AuthenticationException {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.senha()));
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas"));
        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new BadCredentialsException("Credenciais invalidas");
        }
        boolean alterado = false;
        if (senhas.precisaMigrar(usuario.getSenha())) {
            usuario.setSenha(senhas.hash(request.senha()));
            alterado = true;
        }
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
            alterado = true;
        }
        if (alterado) {
            usuario = usuarios.save(usuario);
        }
        return new AuthResponse(UsuarioResponse.from(usuario), jwtService.gerarToken(usuario));
    }
}
