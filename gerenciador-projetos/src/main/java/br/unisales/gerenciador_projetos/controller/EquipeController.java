package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.EquipeRequestDTO;
import br.unisales.gerenciador_projetos.dto.EquipeResponseDTO;
import br.unisales.gerenciador_projetos.dto.EquipeResumoDTO;
import br.unisales.gerenciador_projetos.entity.Equipe;
import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.exception.ResourceNotFoundException;
import br.unisales.gerenciador_projetos.repository.EquipeRepository;
import br.unisales.gerenciador_projetos.service.ProjetoService;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeRepository repository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public EquipeController(EquipeRepository repository,
                            ProjetoService projetoService,
                            UsuarioService usuarioService) {
        this.repository = repository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<EquipeResumoDTO>> listar() {
        List<EquipeResumoDTO> equipes = repository.findAll().stream()
                .map(EquipeResumoDTO::de)
                .toList();
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(EquipeResponseDTO.de(buscar(id)));
    }

    @PostMapping
    public ResponseEntity<EquipeResponseDTO> criar(@RequestBody @Valid EquipeRequestDTO dto) {
        Equipe salva = repository.save(montar(dto));
        URI local = URI.create("/api/equipes/" + salva.getIdEquipe());
        return ResponseEntity.created(local).body(EquipeResponseDTO.de(salva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid EquipeRequestDTO dto) {
        Equipe existente = buscar(id);
        Equipe dados = montar(dto);

        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setProjeto(dados.getProjeto());
        existente.setUsuario(dados.getUsuario());

        return ResponseEntity.ok(EquipeResponseDTO.de(repository.save(existente)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(buscar(id));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Equipe buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe " + id + " não encontrada"));
    }

    private Equipe montar(EquipeRequestDTO dto) {
        Projeto projeto = dto.idProjeto() != null
                ? projetoService.buscarPorId(dto.idProjeto())
                : null;
        Usuario usuario = dto.idUsuario() != null
                ? usuarioService.buscarPorId(dto.idUsuario())
                : null;
        return dto.paraEntidade(projeto, usuario);
    }
}
