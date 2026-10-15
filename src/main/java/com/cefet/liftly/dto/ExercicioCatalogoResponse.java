package com.cefet.liftly.dto;

public record ExercicioCatalogoResponse(
        String id,
        String nome,
        String grupoMuscular,
        String descricao,
        String equipamento
) { }
