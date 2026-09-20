package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Projeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjetoRequestDTO(

        @NotBlank(message = "O nome do projeto é obrigatório")
        @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
        String nome,

        String supervisor,

        String descricao,

        LocalDate dataInicio,

        LocalDate dataFim,

        String status
) {

    public Projeto paraEntidade() {
        Projeto projeto = new Projeto();
        projeto.setNome(nome);
        projeto.setSupervisor(supervisor);
        projeto.setDescricao(descricao);
        projeto.setDataInicio(dataInicio);
        projeto.setDataFim(dataFim);
        projeto.setStatus(status);
        return projeto;
    }
}
