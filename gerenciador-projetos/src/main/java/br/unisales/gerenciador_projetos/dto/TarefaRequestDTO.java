package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TarefaRequestDTO(

        @NotBlank(message = "O título da tarefa é obrigatório")
        String titulo,

        String descricao,

        LocalDate dataInicio,

        LocalDate prazo,

        String prioridade,

        String status,

        @NotNull(message = "A tarefa precisa estar vinculada a um projeto")
        Long idProjeto,

        Long idResponsavel
) {

    public Tarefa paraEntidade(Projeto projeto, Usuario responsavel) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(descricao);
        tarefa.setDataInicio(dataInicio);
        tarefa.setPrazo(prazo);
        tarefa.setPrioridade(prioridade);
        tarefa.setStatus(status);
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(responsavel);
        return tarefa;
    }
}
