package br.unisales.gerenciador_projetos.repository;

import br.unisales.gerenciador_projetos.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByProjetoIdProjeto(Long idProjeto);
    List<Tarefa> findByResponsavelId(Long idResponsavel);

}