package com.cefet.liftly.model;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Data public class ArquivoUsuario {
 @Id private String id;
 private String usuarioId;
 private String nome;
 private String tipo;
 @Column(columnDefinition="bytea") private byte[] dados;
}
