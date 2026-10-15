package com.cefet.liftly.dto;

import com.cefet.liftly.model.Usuario;

public record UsuarioResponse(
        String id,
        String nome,
        String email,
        Double peso,
        Double altura,
        String dataNascimento,
        String objetivo,
        Boolean ativo
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPeso(), usuario.getAltura(),
                usuario.getDataNascimento(), usuario.getObjetivo(), usuario.getAtivo()
        );
    }
}
