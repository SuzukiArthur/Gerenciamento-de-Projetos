package br.unisales.gerenciador_projetos.repository;

import br.unisales.gerenciador_projetos.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {
}