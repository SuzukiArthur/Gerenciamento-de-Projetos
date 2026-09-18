package br.unisales.gerenciadorprojetos.repository;

import br.unisales.gerenciadorprojetos.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
}