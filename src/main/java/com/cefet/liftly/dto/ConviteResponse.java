package com.cefet.liftly.dto;

public record ConviteResponse(
        String id,
        String grupoId,
        String grupoNome,
        String deUsuarioNome,
        String dataEnvio
) { }
