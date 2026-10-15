package com.cefet.liftly.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Convite {
    @Id
    private String id;

    @NotBlank
    private String grupoId;
    private String grupoNome;

    @NotBlank
    private String deUsuarioId;
    private String deUsuarioNome;

    @NotBlank
    private String paraUsuarioId;

    private String status = "PENDENTE";
    private String dataEnvio;
}
