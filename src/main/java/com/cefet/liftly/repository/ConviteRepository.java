package com.cefet.liftly.repository;

import com.cefet.liftly.model.Convite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConviteRepository extends JpaRepository<Convite, String> {
    List<Convite> findByParaUsuarioIdAndStatusOrderByDataEnvioDesc(String paraUsuarioId, String status);
    boolean existsByGrupoIdAndParaUsuarioIdAndStatus(String grupoId, String paraUsuarioId, String status);
}
