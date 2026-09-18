package br.unisales.gerenciador_projetos.service;

import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public List<Tarefa> listarTodos() {
        return repository.findAll();
    }

    public Tarefa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
    }

    public Tarefa salvar(Tarefa tarefa) {
        return repository.save(tarefa);
    }

    public Tarefa atualizar(Long id, Tarefa tarefa) {

        Tarefa tarefaExistente = buscarPorId(id);

        tarefaExistente.setTitulo(tarefa.getTitulo());
        tarefaExistente.setDescricao(tarefa.getDescricao());
        tarefaExistente.setDataInicio(tarefa.getDataInicio());
        tarefaExistente.setPrazo(tarefa.getPrazo());
        tarefaExistente.setPrioridade(tarefa.getPrioridade());
        tarefaExistente.setStatus(tarefa.getStatus());
        tarefaExistente.setProjeto(tarefa.getProjeto());
        tarefaExistente.setResponsavel(tarefa.getResponsavel());

        return repository.save(tarefaExistente);
    }

    public void excluir(Long id) {
        Tarefa tarefa = buscarPorId(id);
        repository.delete(tarefa);
    }

    public List<Tarefa> listarPorProjeto(Long idProjeto) {
        return repository.findByProjetoIdProjeto(idProjeto);
    }

    public List<Tarefa> listarPorResponsavel(Long idResponsavel) {
        return repository.findByResponsavelId(idResponsavel);
    }
}