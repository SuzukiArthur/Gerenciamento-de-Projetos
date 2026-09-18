package br.unisales.gerenciadorprojetos.repository;

import br.unisales.gerenciadorprojetos.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {
}