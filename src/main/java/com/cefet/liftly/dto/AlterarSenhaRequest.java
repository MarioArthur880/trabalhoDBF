package com.cefet.liftly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
        @NotBlank @Size(max = 200) String senhaAtual,
        @NotBlank @Size(min = 6, max = 200) String novaSenha
) {}
