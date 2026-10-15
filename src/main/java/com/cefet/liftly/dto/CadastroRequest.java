package com.cefet.liftly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank @Size(min = 3, max = 100) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 200) String senha
) { }
