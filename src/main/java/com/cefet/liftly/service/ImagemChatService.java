package com.cefet.liftly.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Service
public class ImagemChatService {
    public String validar(String imagem) {
        if (imagem == null || imagem.isBlank()) return null;
        try {
            if (!imagem.startsWith("data:image/jpeg;base64,") && !imagem.startsWith("data:image/png;base64,"))
                throw new IllegalArgumentException();
            byte[] bytes = Base64.getDecoder().decode(imagem.substring(imagem.indexOf(',') + 1));
            if (bytes.length == 0 || bytes.length > 500000) throw new IllegalArgumentException();
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IllegalArgumentException();
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    String formato = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                    if (!imagem.startsWith("data:image/" + (formato.equals("jpeg") ? "jpeg" : "png") + ";base64,")
                            || (!formato.equals("jpeg") && !formato.equals("png"))) throw new IllegalArgumentException();
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 16000000) throw new IllegalArgumentException();
                    if (reader.read(0) == null) throw new IllegalArgumentException();
                } finally { reader.dispose(); }
            }
            return imagem;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma foto JPEG ou PNG de até 500 KB");
        }
    }
}
