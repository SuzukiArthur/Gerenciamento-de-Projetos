package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Equipe;
import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.entity.Usuario;
import jakarta.validation.constraints.NotBlank;

public record EquipeRequestDTO(

        @NotBlank(message = "O nome da equipe é obrigatório")
        String nome,

        String descricao,

        Long idProjeto,

        Long idUsuario
) {

    public Equipe paraEntidade(Projeto projeto, Usuario usuario) {
        Equipe equipe = new Equipe();
        equipe.setNome(nome);
        equipe.setDescricao(descricao);
        equipe.setProjeto(projeto);
        equipe.setUsuario(usuario);
        return equipe;
    }
}
