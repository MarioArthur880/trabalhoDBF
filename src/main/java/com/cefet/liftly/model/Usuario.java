package com.cefet.liftly.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Usuario {
    @Id
    private String id;

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    private Double peso;
    private Double altura;
    private String dataNascimento;
    private String objetivo;
    private Boolean ativo = true;
    @jakarta.persistence.Column(nullable = false)
    private String perfil = "USER";
}
