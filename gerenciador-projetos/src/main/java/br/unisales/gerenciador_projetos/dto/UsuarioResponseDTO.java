package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Usuario;

public record UsuarioResponseDTO(Long id, String nome, String login, String funcao) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getFuncao()
        );
    }
}
