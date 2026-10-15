package com.cefet.liftly.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Divisao {
    @Id
    private String id;

    @NotBlank
    private String usuarioId;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 500)
    private String descricao;
    private String dataCriacao;

    @Valid
    @OneToMany(
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER)
    @JoinColumn(name = "divisao_id")
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<ExercicioTreino> exercicios = new ArrayList<>();
}
