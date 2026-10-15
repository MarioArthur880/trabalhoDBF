package com.cefet.liftly.dto;

public record GrupoResponse(
        String id,
        String nome,
        String codigoConvite,
        String criadorId,
        int totalMembros
) { }
