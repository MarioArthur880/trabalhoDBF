package com.cefet.liftly.repository;

import com.cefet.liftly.model.MensagemGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemGrupoRepository extends JpaRepository<MensagemGrupo, String> {
    List<MensagemGrupo> findTop100ByGrupoIdOrderByDataEnvioDesc(String grupoId);
}
