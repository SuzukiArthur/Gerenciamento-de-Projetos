package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Usuario;

public record UsuarioResumoDTO(Long id, String nome, String login) {

    public static UsuarioResumoDTO de(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getLogin());
    }
}
