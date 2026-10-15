package com.cefet.liftly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "mensagem_grupo", indexes = {
        @Index(name = "idx_mensagem_grupo_data", columnList = "grupo_id,data_envio"),
        @Index(name = "idx_mensagem_autor", columnList = "autor_id")
})
@Data
public class MensagemGrupo {
    @Id
    private String id;

    @Column(name = "grupo_id", nullable = false)
    private String grupoId;

    @Column(name = "autor_id", nullable = false)
    private String autorId;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(name = "imagem", columnDefinition = "text")
    private String imagem;

    @Column(name = "data_envio", nullable = false)
    private Instant dataEnvio;
}
