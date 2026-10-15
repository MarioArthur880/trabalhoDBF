package com.cefet.liftly.service;

import com.cefet.liftly.model.PesoRegistro;
import com.cefet.liftly.model.Usuario;
import com.cefet.liftly.repository.PesoRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PesoService {
    private final PesoRegistroRepository pesoRegistroRepository;
    private final IdempotenciaService idempotenciaService;

    @Transactional
    public void registrarAlteracao(Usuario usuario, Double novoPeso) {
        idempotenciaService.reservar("peso", usuario.getId(), "weight-guard-000000");
        idempotenciaService.atualizar(usuario);
        Double pesoAnterior = usuario.getPeso();
        if (mesmoPeso(pesoAnterior, novoPeso)) return;

        Instant agora = Instant.now();
        if (pesoAnterior != null && !pesoRegistroRepository.existsByUsuarioId(usuario.getId())) {
            salvar(usuario.getId(), pesoAnterior, agora.minusMillis(1));
        }
        if (novoPeso != null) {
            salvar(usuario.getId(), novoPeso, agora);
        }
    }

    @Transactional
    public List<PesoRegistro> listarComBaseline(Usuario usuario) {
        idempotenciaService.reservar("peso", usuario.getId(), "weight-guard-000000");
        idempotenciaService.atualizar(usuario);
        List<PesoRegistro> registros = pesoRegistroRepository.findByUsuarioIdOrderByDataRegistroAsc(usuario.getId());
        if (usuario.getPeso() == null) return registros;

        if (registros.isEmpty()) {
            salvar(usuario.getId(), usuario.getPeso(), Instant.now());
            return pesoRegistroRepository.findByUsuarioIdOrderByDataRegistroAsc(usuario.getId());
        }

        PesoRegistro ultimo = registros.get(registros.size() - 1);
        if (!mesmoPeso(ultimo.getPeso(), usuario.getPeso())) {
            salvar(usuario.getId(), usuario.getPeso(), Instant.now());
            return pesoRegistroRepository.findByUsuarioIdOrderByDataRegistroAsc(usuario.getId());
        }
        return registros;
    }

    private void salvar(String usuarioId, Double peso, Instant data) {
        PesoRegistro registro = new PesoRegistro();
        registro.setId(UUID.randomUUID().toString());
        registro.setUsuarioId(usuarioId);
        registro.setPeso(peso);
        registro.setDataRegistro(data);
        pesoRegistroRepository.save(registro);
    }

    private boolean mesmoPeso(Double a, Double b) {
        if (a == null || b == null) return a == b;
        return Math.abs(a - b) < 0.0001;
    }
}
