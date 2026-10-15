package com.cefet.liftly.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Grupo {
    @Id
    private String id;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank
    private String criadorId;

    private String codigoConvite;
    private String dataCriacao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "grupo_membros", joinColumns = @JoinColumn(name = "grupo_id"))
    @Column(name = "usuario_id")
    private List<String> membrosIds = new ArrayList<>();
}
