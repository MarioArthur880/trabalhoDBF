package com.cefet.liftly.dto;
import jakarta.validation.constraints.Size;
public record MensagemRequest(@Size(max = 1000) String texto,
                              @Size(max = 700000) String imagem) { }
