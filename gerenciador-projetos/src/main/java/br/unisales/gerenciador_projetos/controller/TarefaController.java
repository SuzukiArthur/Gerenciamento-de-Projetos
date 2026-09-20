package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.TarefaRequestDTO;
import br.unisales.gerenciador_projetos.dto.TarefaResponseDTO;
import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.service.ProjetoService;
import br.unisales.gerenciador_projetos.service.TarefaService;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService service;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public TarefaController(TarefaService service,
                            ProjetoService projetoService,
                            UsuarioService usuarioService) {
        this.service = service;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    /**
     * GET /api/tarefas
     * GET /api/tarefas?projetoId=1
     * GET /api/tarefas?responsavelId=3
     */
    @GetMapping
    public ResponseEntity<List<TarefaResponseDTO>> listar(
            @RequestParam(required = false) Long projetoId,
            @RequestParam(required = false) Long responsavelId) {

        List<Tarefa> tarefas;
        if (projetoId != null) {
            tarefas = service.listarPorProjeto(projetoId);
        } else if (responsavelId != null) {
            tarefas = service.listarPorResponsavel(responsavelId);
        } else {
            tarefas = service.listarTodos();
        }

        return ResponseEntity.ok(tarefas.stream().map(TarefaResponseDTO::de).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(TarefaResponseDTO.de(service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criar(@RequestBody @Valid TarefaRequestDTO dto) {
        Tarefa salva = service.salvar(montar(dto));
        URI local = URI.create("/api/tarefas/" + salva.getIdTarefa());
        return ResponseEntity.created(local).body(TarefaResponseDTO.de(salva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid TarefaRequestDTO dto) {
        Tarefa atualizada = service.atualizar(id, montar(dto));
        return ResponseEntity.ok(TarefaResponseDTO.de(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Tarefa montar(TarefaRequestDTO dto) {
        Projeto projeto = projetoService.buscarPorId(dto.idProjeto());
        Usuario responsavel = dto.idResponsavel() != null
                ? usuarioService.buscarPorId(dto.idResponsavel())
                : null;
        return dto.paraEntidade(projeto, responsavel);
    }
}
