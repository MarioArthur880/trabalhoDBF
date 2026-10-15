package com.cefet.liftly.controller;

import com.cefet.liftly.dto.MensagemRequest;
import com.cefet.liftly.model.*;
import com.cefet.liftly.repository.*;
import com.cefet.liftly.service.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MensagemGrupoControllerTests {
    private final GrupoRepository grupos = mock(GrupoRepository.class);
    private final MensagemGrupoRepository mensagens = mock(MensagemGrupoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final IdempotenciaService keys = mock(IdempotenciaService.class);
    private final ImagemChatService imagens = mock(ImagemChatService.class);
    private final MensagemGrupoController controller = new MensagemGrupoController(grupos, mensagens, usuarios, keys, imagens);
    private void membro() {
        Grupo grupo = new Grupo(); grupo.setMembrosIds(new ArrayList<>(List.of("u")));
        when(grupos.findById("g")).thenReturn(Optional.of(grupo));
        when(usuarios.findById("u")).thenReturn(Optional.empty());
    }
    @Test void reenvioRetornaRegistroExistenteSemSalvarDeNovo() {
        membro();
        MensagemGrupo m = new MensagemGrupo(); m.setId("m"); m.setGrupoId("g"); m.setAutorId("u");
        m.setTexto("Olá"); m.setDataEnvio(Instant.now());
        when(keys.reservar("mensagens:g", "u", "chave-000000000000")).thenReturn("m");
        when(mensagens.findById("m")).thenReturn(Optional.of(m));
        var resposta = controller.enviar("g", "u", "chave-000000000000", new MensagemRequest("Olá", null));
        assertEquals(200, resposta.getStatusCode().value());
        assertEquals("m", resposta.getBody().id());
        verify(mensagens, never()).save(any());
    }
    @Test void aceitaFotoSemLegenda() {
        membro();
        when(imagens.validar("foto")).thenReturn("foto");
        when(keys.reservar(anyString(), anyString(), any())).thenReturn("m");
        when(mensagens.findById("m")).thenReturn(Optional.empty());
        when(mensagens.save(any())).thenAnswer(i -> i.getArgument(0));
        var resposta = controller.enviar("g", "u", null, new MensagemRequest("", "foto"));
        assertEquals(201, resposta.getStatusCode().value());
        assertEquals("foto", resposta.getBody().imagem());
    }
    @Test void impedeNaoMembroEMensagemVazia() {
        when(grupos.findById("g")).thenReturn(Optional.empty());
        assertEquals(403, controller.enviar("g", "u", null, new MensagemRequest("Olá", null)).getStatusCode().value());
        membro();
        assertEquals(400, controller.enviar("g", "u", null, new MensagemRequest(" ", null)).getStatusCode().value());
        verify(mensagens, never()).save(any());
    }
}
