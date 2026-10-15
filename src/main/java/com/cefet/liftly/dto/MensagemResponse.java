package com.cefet.liftly.dto;

import java.time.Instant;

public record MensagemResponse(
        String id,
        String grupoId,
        String autorId,
        String autorNome,
        String texto,
        String imagem,
        Instant dataEnvio
) { }
