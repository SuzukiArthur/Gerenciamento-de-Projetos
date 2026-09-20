package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Equipe;

public record EquipeResponseDTO(
        Long idEquipe,
        String nome,
        String descricao,
        ProjetoResumoDTO projeto,
        UsuarioResumoDTO usuario
) {

    public static EquipeResponseDTO de(Equipe equipe) {
        if (equipe == null) {
            return null;
        }
        return new EquipeResponseDTO(
                equipe.getIdEquipe(),
                equipe.getNome(),
                equipe.getDescricao(),
                ProjetoResumoDTO.de(equipe.getProjeto()),
                UsuarioResumoDTO.de(equipe.getUsuario())
        );
    }
}