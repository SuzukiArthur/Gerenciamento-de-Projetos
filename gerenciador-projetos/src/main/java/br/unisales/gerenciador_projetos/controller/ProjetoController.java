package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.*;
import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.repository.ProjetoRepository;
import br.unisales.gerenciador_projetos.service.ProjetoService;
import br.unisales.gerenciador_projetos.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoService service;
    private final TarefaService tarefaService;
    private final ProjetoRepository repository; // usado apenas para a paginação

    public ProjetoController(ProjetoService service,
                             TarefaService tarefaService,
                             ProjetoRepository repository) {
        this.service = service;
        this.tarefaService = tarefaService;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<ProjetoResumoDTO>> listar() {
        List<ProjetoResumoDTO> projetos = service.listarTodos().stream()
                .map(ProjetoResumoDTO::de)
                .toList();
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/paginado")
    public ResponseEntity<PageResponseDTO<ProjetoResumoDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(defaultValue = "idProjeto") String ordenarPor) {

        var page = repository.findAll(PageRequest.of(pagina, tamanho, Sort.by(ordenarPor)));
        return ResponseEntity.ok(PageResponseDTO.de(page, ProjetoResumoDTO::de));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ProjetoResponseDTO.de(service.buscarPorId(id)));
    }

    @GetMapping("/{id}/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefas(@PathVariable Long id) {
        service.buscarPorId(id); // garante que o projeto existe
        List<TarefaResponseDTO> tarefas = tarefaService.listarPorProjeto(id).stream()
                .map(TarefaResponseDTO::de)
                .toList();
        return ResponseEntity.ok(tarefas);
    }

    @PostMapping
    public ResponseEntity<ProjetoResponseDTO> criar(@RequestBody @Valid ProjetoRequestDTO dto) {
        Projeto salvo = service.salvar(dto.paraEntidade());
        URI local = URI.create("/api/projetos/" + salvo.getIdProjeto());
        return ResponseEntity.created(local).body(ProjetoResponseDTO.de(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid ProjetoRequestDTO dto) {
        Projeto atualizado = service.atualizar(id, dto.paraEntidade());
        return ResponseEntity.ok(ProjetoResponseDTO.de(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
