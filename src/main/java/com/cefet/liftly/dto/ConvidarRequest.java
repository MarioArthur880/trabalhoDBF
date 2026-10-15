package com.cefet.liftly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConvidarRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 100) String deUsuarioId
) { }
