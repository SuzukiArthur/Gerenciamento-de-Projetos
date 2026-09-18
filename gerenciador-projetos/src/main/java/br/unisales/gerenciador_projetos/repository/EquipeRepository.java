package br.unisales.gerenciador_projetos.repository;

import br.unisales.gerenciador_projetos.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
}