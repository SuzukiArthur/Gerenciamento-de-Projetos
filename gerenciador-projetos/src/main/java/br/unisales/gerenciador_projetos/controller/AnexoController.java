package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.AnexoRequestDTO;
import br.unisales.gerenciador_projetos.dto.AnexoResponseDTO;
import br.unisales.gerenciador_projetos.entity.Anexo;
import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.service.AnexoService;
import br.unisales.gerenciador_projetos.service.TarefaService;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/anexos")
public class AnexoController {

    private final AnexoService service;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;

    public AnexoController(AnexoService service,
                           TarefaService tarefaService,
                           UsuarioService usuarioService) {
        this.service = service;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
    }

    /** GET /api/anexos  ou  GET /api/anexos?tarefaId=5 */
    @GetMapping
    public ResponseEntity<List<AnexoResponseDTO>> listar(@RequestParam(required = false) Long tarefaId) {
        List<Anexo> anexos = (tarefaId != null)
                ? service.listarPorTarefa(tarefaId)
                : service.listarTodos();
        return ResponseEntity.ok(anexos.stream().map(AnexoResponseDTO::de).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnexoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(AnexoResponseDTO.de(service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<AnexoResponseDTO> criar(@RequestBody @Valid AnexoRequestDTO dto) {
        Anexo salvo = service.salvar(montar(dto));
        URI local = URI.create("/api/anexos/" + salvo.getId());
        return ResponseEntity.created(local).body(AnexoResponseDTO.de(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnexoResponseDTO> atualizar(@PathVariable Long id,
                                                      @RequestBody @Valid AnexoRequestDTO dto) {
        Anexo atualizado = service.atualizar(id, montar(dto));
        return ResponseEntity.ok(AnexoResponseDTO.de(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Anexo montar(AnexoRequestDTO dto) {
        Tarefa tarefa = tarefaService.buscarPorId(dto.idTarefa());
        Usuario usuario = dto.idUsuario() != null
                ? usuarioService.buscarPorId(dto.idUsuario())
                : null;
        return dto.paraEntidade(tarefa, usuario);
    }
}