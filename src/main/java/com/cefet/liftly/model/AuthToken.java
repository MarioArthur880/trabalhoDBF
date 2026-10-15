package com.cefet.liftly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "auth_token", indexes = @Index(name = "idx_auth_token_usuario", columnList = "usuario_id"))
@Data
public class AuthToken {
    @Id
    @Column(length = 80)
    private String token;

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;
}
