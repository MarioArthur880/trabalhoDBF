package com.cefet.liftly.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class ExercicioTreino {
    @Id
    private String id;

    private String catalogoId;

    @NotBlank
    @Size(max = 160)
    private String nome;
    @Size(max = 80)
    private String grupoMuscular;

    @NotNull
    @Min(1)
    private Integer series;

    @NotNull
    @Min(1)
    private Integer repeticoes;

    @NotNull
    @Min(0)
    private Double carga;

    @Min(0)
    private Integer descansoSegundos;

    @Size(max = 300)
    private String observacao;
}
