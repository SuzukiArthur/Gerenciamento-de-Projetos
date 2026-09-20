package br.unisales.gerenciador_projetos.dto;

import br.unisales.gerenciador_projetos.entity.Equipe;

public record EquipeResumoDTO(Long idEquipe, String nome) {

    public static EquipeResumoDTO de(Equipe equipe) {
        if (equipe == null) {
            return null;
        }
        return new EquipeResumoDTO(equipe.getIdEquipe(), equipe.getNome());
    }
}
