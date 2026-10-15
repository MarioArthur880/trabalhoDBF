package com.cefet.liftly.security;

import com.cefet.liftly.config.AuthInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Lê Bearer JWT, autentica usuário e mantém usuarioAutenticadoId para os endpoints Liftly. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarToken(request);
        if (token != null && jwtService.validarToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtService.getLogin(token);
                UsuarioDetailsService.UsuarioAutenticado usuario =
                        (UsuarioDetailsService.UsuarioAutenticado) usuarioDetailsService.loadUserByUsername(email);
                if (usuario.isEnabled()) {
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(autenticacao);
                    SecurityContextHolder.setContext(context);
                    request.setAttribute(AuthInterceptor.USUARIO_ID_ATTR, usuario.getId());
                }
            } catch (UsernameNotFoundException e) {
                // Usuário excluído: a requisição segue sem autenticação (401).
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
