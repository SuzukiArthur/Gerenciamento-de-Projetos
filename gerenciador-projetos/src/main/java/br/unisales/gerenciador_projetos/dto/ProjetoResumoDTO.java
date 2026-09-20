package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Projeto;

public record ProjetoResumoDTO(Long idProjeto, String nome, String status) {

    public static ProjetoResumoDTO de(Projeto projeto) {
        if (projeto == null) {
            return null;
        }
        return new ProjetoResumoDTO(projeto.getIdProjeto(), projeto.getNome(), projeto.getStatus());
    }
}
