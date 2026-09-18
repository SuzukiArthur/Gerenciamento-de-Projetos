package br.unisales.gerenciadorprojetos.repository;

import br.unisales.gerenciadorprojetos.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
}