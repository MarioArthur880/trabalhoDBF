package com.cefet.liftly.repository;

import com.cefet.liftly.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoRepository extends JpaRepository<Historico, String> {
    List<Historico> findByUsuarioIdOrderByDataInicioDesc(String usuarioId);
}
