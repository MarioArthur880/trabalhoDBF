package com.cefet.liftly.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.FetchType;

@Entity
@Data
public class HistoricoExercicio {
    @Id
    private String id;

    private String exercicioId;
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
    private Boolean avulso = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<Boolean> seriesFeitas = new ArrayList<>();
}
