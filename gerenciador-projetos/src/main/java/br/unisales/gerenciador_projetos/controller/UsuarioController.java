package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.UsuarioRequestDTO;
import br.unisales.gerenciador_projetos.dto.UsuarioResponseDTO;
import br.unisales.gerenciador_projetos.dto.UsuarioResumoDTO;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResumoDTO>> listar() {
        List<UsuarioResumoDTO> usuarios = service.listarTodos().stream()
                .map(UsuarioResumoDTO::de)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponseDTO.de(service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario salvo = service.salvar(dto.paraEntidade());
        URI local = URI.create("/api/usuarios/" + salvo.getId());
        return ResponseEntity.created(local).body(UsuarioResponseDTO.de(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario atualizado = service.atualizar(id, dto.paraEntidade());
        return ResponseEntity.ok(UsuarioResponseDTO.de(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
