package com.cefet.liftly.dto;

import java.time.Instant;

public record PesoRegistroResponse(String id, Double peso, Instant dataRegistro) { }
