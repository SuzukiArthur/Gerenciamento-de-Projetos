package br.unisales.gerenciador_projetos.repository;

import br.unisales.gerenciador_projetos.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
}