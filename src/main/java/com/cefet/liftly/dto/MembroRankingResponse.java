package com.cefet.liftly.dto;

public record MembroRankingResponse(
        String usuarioId,
        String nome,
        int streakAtual,
        int treinosNaSemana
) { }
