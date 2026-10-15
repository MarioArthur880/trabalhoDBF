package com.cefet.liftly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "peso_registro", indexes = @Index(name = "idx_peso_usuario_data", columnList = "usuario_id,data_registro"))
@Data
public class PesoRegistro {
    @Id
    private String id;

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private Double peso;

    @Column(name = "data_registro", nullable = false)
    private Instant dataRegistro;
}
