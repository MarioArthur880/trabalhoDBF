package com.cefet.liftly.repository;

import com.cefet.liftly.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, String> {
    Optional<Grupo> findByCodigoConvite(String codigoConvite);

    @Query("SELECT g FROM Grupo g JOIN g.membrosIds m WHERE m = :usuarioId")
    List<Grupo> findByMembro(@Param("usuarioId") String usuarioId);
}
