package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Anexo;
import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnexoRequestDTO(

        @NotNull(message = "O anexo precisa estar vinculado a uma tarefa")
        Long idTarefa,

        @NotBlank(message = "O nome do arquivo é obrigatório")
        String nomeArquivo,

        @NotBlank(message = "A URL do arquivo é obrigatória")
        String urlArquivo,

        String tipoArquivo,

        Long idUsuario
) {

    public Anexo paraEntidade(Tarefa tarefa, Usuario usuario) {
        Anexo anexo = new Anexo();
        anexo.setTarefa(tarefa);
        anexo.setNomeArquivo(nomeArquivo);
        anexo.setUrlArquivo(urlArquivo);
        anexo.setTipoArquivo(tipoArquivo);
        anexo.setUsuario(usuario);
        return anexo;
    }
}
