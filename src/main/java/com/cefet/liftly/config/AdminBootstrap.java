package com.cefet.liftly.config;
import com.cefet.liftly.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component public class AdminBootstrap implements CommandLineRunner {
 private final UsuarioRepository usuarios; private final String email;
 public AdminBootstrap(UsuarioRepository usuarios,@Value("${ADMIN_EMAIL:}") String email){this.usuarios=usuarios;this.email=email;}
 public void run(String... args){if(!email.isBlank()) usuarios.findByEmailIgnoreCase(email.trim()).ifPresent(u->{u.setPerfil("ADMIN");usuarios.save(u);});}
}
