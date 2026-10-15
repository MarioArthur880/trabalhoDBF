package com.cefet.liftly.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @NotBlank @Size(min = 3, max = 100) String nome,
        @Size(max = 30) String dataNascimento,
        @DecimalMin("30.0") @DecimalMax("300.0") Double peso,
        @DecimalMin("100.0") @DecimalMax("250.0") Double altura,
        @Size(max = 300) String objetivo
) { }
