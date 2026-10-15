package com.cefet.liftly.controller;

import com.cefet.liftly.config.AuthInterceptor;
import com.cefet.liftly.dto.PesoRegistroResponse;
import com.cefet.liftly.repository.UsuarioRepository;
import com.cefet.liftly.service.PesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pesos")
@RequiredArgsConstructor
public class PesoController {
    private final UsuarioRepository usuarioRepository;
    private final PesoService pesoService;

    @GetMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<List<PesoRegistroResponse>> listar(
            @RequestAttribute(AuthInterceptor.USUARIO_ID_ATTR) String usuarioAutenticadoId) {
        return usuarioRepository.findById(usuarioAutenticadoId)
                .map(usuario -> ResponseEntity.ok(pesoService.listarComBaseline(usuario).stream()
                        .map(p -> new PesoRegistroResponse(p.getId(), p.getPeso(), p.getDataRegistro()))
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }
}
