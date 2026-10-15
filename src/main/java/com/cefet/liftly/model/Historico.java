package com.cefet.liftly.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Historico {
    @Id
    private String id;

    @NotBlank
    private String usuarioId;
    private String divisaoId;
    private String divisaoNome;

    @NotBlank
    private String dataInicio;

    @NotBlank
    private String dataFim;

    @Valid
    @NotEmpty
    @OneToMany(
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER)
    @JoinColumn(name = "historico_id")
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<HistoricoExercicio> exercicios = new ArrayList<>();
}
