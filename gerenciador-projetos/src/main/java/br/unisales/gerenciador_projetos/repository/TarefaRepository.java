package br.unisales.gerenciadorprojetos.repository;

import br.unisales.gerenciadorprojetos.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
}