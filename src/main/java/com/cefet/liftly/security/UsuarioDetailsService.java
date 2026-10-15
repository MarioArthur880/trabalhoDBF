package com.cefet.liftly.security;

import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Carrega usuários reais do Neon para o Spring Security. */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return new UsuarioAutenticado(usuario);
    }

    /** O ID continua disponível aos controllers, como no Liftly original. */
    public static class UsuarioAutenticado extends User {
        private final String id;

        public UsuarioAutenticado(Usuario usuario) {
            super(usuario.getEmail(), usuario.getSenha(),
                    !Boolean.FALSE.equals(usuario.getAtivo()), true, true, true,
                    List.of(new SimpleGrantedAuthority("ROLE_" +
                            ("ADMIN".equals(usuario.getPerfil()) ? "ADMIN" : "USER"))));
            this.id = usuario.getId();
        }

        public String getId() {
            return id;
        }
    }
}
