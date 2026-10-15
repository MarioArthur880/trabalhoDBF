package com.cefet.liftly.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class ImagemChatServiceTests {
    private final ImagemChatService service = new ImagemChatService();
    @Test void aceitaFotoReal() throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "jpeg", output);
        String foto = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        assertEquals(foto, service.validar(foto));
    }
    @Test void rejeitaConteudoFalsoESvg() {
        assertThrows(ResponseStatusException.class, () -> service.validar("data:image/jpeg;base64,SGVsbG8="));
        assertThrows(ResponseStatusException.class, () -> service.validar("data:image/svg+xml;base64,SGVsbG8="));
        assertThrows(ResponseStatusException.class, () -> service.validar("data:image/png;base64,%%%"));
    }
    @Test void rejeitaArquivoAcimaDoLimite() {
        String foto = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[500001]);
        assertThrows(ResponseStatusException.class, () -> service.validar(foto));
    }
    @Test void permiteMensagemSemFoto() {
        assertNull(service.validar(null));
        assertNull(service.validar(""));
    }
}
