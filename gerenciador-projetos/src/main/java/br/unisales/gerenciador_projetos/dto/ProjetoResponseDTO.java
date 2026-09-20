package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Projeto;

import java.time.LocalDate;

public record ProjetoResponseDTO(
        Long idProjeto,
        String nome,
        String supervisor,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        String status
) {

    public static ProjetoResponseDTO de(Projeto projeto) {
        if (projeto == null) {
            return null;
        }
        return new ProjetoResponseDTO(
                projeto.getIdProjeto(),
                projeto.getNome(),
                projeto.getSupervisor(),
                projeto.getDescricao(),
                projeto.getDataInicio(),
                projeto.getDataFim(),
                projeto.getStatus()
        );
    }
}
