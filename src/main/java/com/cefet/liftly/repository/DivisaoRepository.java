package com.cefet.liftly.repository;

import com.cefet.liftly.model.Divisao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DivisaoRepository extends JpaRepository<Divisao, String> {
    List<Divisao> findByUsuarioId(String usuarioId);
}
