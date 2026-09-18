package br.unisales.gerenciador_projetos.service;

import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.repository.ProjetoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository repository;

    public ProjetoService(ProjetoRepository repository) {
        this.repository = repository;
    }

    public List<Projeto> listarTodos() {
        return repository.findAll();
    }

    public Projeto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
    }

    public Projeto salvar(Projeto projeto) {
        return repository.save(projeto);
    }

    public Projeto atualizar(Long id, Projeto projeto) {

        Projeto projetoExistente = buscarPorId(id);

        projetoExistente.setNome(projeto.getNome());
        projetoExistente.setSupervisor(projeto.getSupervisor());
        projetoExistente.setDescricao(projeto.getDescricao());
        projetoExistente.setDataInicio(projeto.getDataInicio());
        projetoExistente.setDataFim(projeto.getDataFim());
        projetoExistente.setStatus(projeto.getStatus());

        return repository.save(projetoExistente);
    }

    public void excluir(Long id) {
        Projeto projeto = buscarPorId(id);
        repository.delete(projeto);
    }
}