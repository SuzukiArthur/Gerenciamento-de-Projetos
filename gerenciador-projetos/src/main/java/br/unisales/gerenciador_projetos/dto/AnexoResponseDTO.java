package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Anexo;

public record AnexoResponseDTO(
        Long id,
        String nomeArquivo,
        String urlArquivo,
        String tipoArquivo,
        Long idTarefa,
        UsuarioResumoDTO usuario
) {

    public static AnexoResponseDTO de(Anexo anexo) {
        if (anexo == null) {
            return null;
        }
        return new AnexoResponseDTO(
                anexo.getId(),
                anexo.getNomeArquivo(),
                anexo.getUrlArquivo(),
                anexo.getTipoArquivo(),
                anexo.getTarefa() != null ? anexo.getTarefa().getIdTarefa() : null,
                UsuarioResumoDTO.de(anexo.getUsuario())
        );
    }
}
