package com.cefet.liftly.security;

import com.cefet.liftly.model.Usuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {
    @Test
    void geraEValidaJwtAssinado() {
        JwtService service = new JwtService("uma-chave-secreta-de-teste-com-mais-de-trinta-e-dois-bytes", 86400000);
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@example.com");
        usuario.setPerfil("USER");
        String token = service.gerarToken(usuario);
        assertEquals(3, token.split("\\.").length);
        assertTrue(service.validarToken(token));
        assertEquals("teste@example.com", service.getLogin(token));
        assertFalse(service.validarToken(token + "alterado"));
    }

    @Test
    void chaveFracaNaoEPermitida() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("fraca", 86400000));
    }
}
