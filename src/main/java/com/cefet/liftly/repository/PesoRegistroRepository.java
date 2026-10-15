package com.cefet.liftly.repository;

import com.cefet.liftly.model.PesoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PesoRegistroRepository extends JpaRepository<PesoRegistro, String> {
    List<PesoRegistro> findByUsuarioIdOrderByDataRegistroAsc(String usuarioId);
    boolean existsByUsuarioId(String usuarioId);
}
