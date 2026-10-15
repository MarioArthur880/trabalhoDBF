package com.cefet.liftly.dto;

import java.util.List;

public record GrupoDetalheResponse(
        String id,
        String nome,
        String codigoConvite,
        List<MembroRankingResponse> ranking
) { }
