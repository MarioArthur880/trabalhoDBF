package com.cefet.liftly.security;

import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioDetailsServiceTests {
    private final UsuarioRepository repo = mock(UsuarioRepository.class);
    private final UsuarioDetailsService service = new UsuarioDetailsService(repo);

    @Test
    void trazIdEAutoridadeDoBanco() {
        Usuario usuario = new Usuario();
        usuario.setId("usuario-1");
        usuario.setEmail("admin@example.com");
        usuario.setSenha("hash");
        usuario.setPerfil("ADMIN");
        when(repo.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(usuario));
        var detalhe = (UsuarioDetailsService.UsuarioAutenticado) service.loadUserByUsername("admin@example.com");
        assertEquals("usuario-1", detalhe.getId());
        assertTrue(detalhe.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void bloqueiaUsuarioDesativado() {
        Usuario usuario = new Usuario();
        usuario.setId("usuario-2");
        usuario.setEmail("inativo@example.com");
        usuario.setSenha("hash");
        usuario.setAtivo(false);
        when(repo.findByEmailIgnoreCase("inativo@example.com")).thenReturn(Optional.of(usuario));
        var detalhe = service.loadUserByUsername("inativo@example.com");
        assertFalse(detalhe.isEnabled());
    }

    @Test
    void rejeitaUsuarioInexistente() {
        when(repo.findByEmailIgnoreCase("desconhecido@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("desconhecido@example.com"));
    }
}
