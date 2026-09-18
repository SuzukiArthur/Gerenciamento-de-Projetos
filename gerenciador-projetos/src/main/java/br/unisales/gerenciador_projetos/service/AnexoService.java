package br.unisales.gerenciador_projetos.service;

import br.unisales.gerenciador_projetos.entity.Anexo;
import br.unisales.gerenciador_projetos.repository.AnexoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnexoService {

    private final AnexoRepository repository;

    public AnexoService(AnexoRepository repository) {
        this.repository = repository;
    }

    public List<Anexo> listarTodos() {
        return repository.findAll();
    }

    public Anexo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));
    }

    public Anexo salvar(Anexo anexo) {
        return repository.save(anexo);
    }

    public Anexo atualizar(Long id, Anexo anexo) {

        Anexo anexoExistente = buscarPorId(id);

        anexoExistente.setTarefa(anexo.getTarefa());
        anexoExistente.setUrlArquivo(anexo.getUrlArquivo());
        anexoExistente.setNomeArquivo(anexo.getNomeArquivo());
        anexoExistente.setTipoArquivo(anexo.getTipoArquivo());
        anexoExistente.setUsuario(anexo.getUsuario());

        return repository.save(anexoExistente);
    }

    public void excluir(Long id) {
        Anexo anexo = buscarPorId(id);
        repository.delete(anexo);
    }

    public List<Anexo> listarPorTarefa(Long idTarefa) {
        return repository.findByTarefaIdTarefa(idTarefa);
    }
}