package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        String funcao
) {

    public Usuario paraEntidade() {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setSenha(senha);
        usuario.setFuncao(funcao);
        return usuario;
    }
}
