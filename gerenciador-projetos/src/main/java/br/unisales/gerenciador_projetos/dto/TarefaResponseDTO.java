package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Tarefa;

import java.time.LocalDate;

public record TarefaResponseDTO(
        Long idTarefa,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate prazo,
        String prioridade,
        String status,
        ProjetoResumoDTO projeto,
        UsuarioResumoDTO responsavel
) {

    public static TarefaResponseDTO de(Tarefa tarefa) {
        if (tarefa == null) {
            return null;
        }
        return new TarefaResponseDTO(
                tarefa.getIdTarefa(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getDataInicio(),
                tarefa.getPrazo(),
                tarefa.getPrioridade(),
                tarefa.getStatus(),
                ProjetoResumoDTO.de(tarefa.getProjeto()),
                UsuarioResumoDTO.de(tarefa.getResponsavel())
        );
    }
}
