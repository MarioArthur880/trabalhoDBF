package com.cefet.liftly.repository;

import com.cefet.liftly.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    void deleteByUsuarioId(String usuarioId);
}
