package br.unisales.gerenciador_projetos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.unisales.gerenciador_projetos.dto.UsuarioRequestDTO;
import br.unisales.gerenciador_projetos.dto.UsuarioResponseDTO;
import br.unisales.gerenciador_projetos.dto.UsuarioResumoDTO;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import jakarta.validation.Valid;

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
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid UsuarioRequestDTO dto,
                                                    Authentication autenticacao) {
        Usuario novo = dto.paraEntidade();

        // O cadastro é público; se não houver autenticação, define a função padrão.
        // Se o usuário autenticado for ADMIN, ele pode escolher a função.
        if (autenticacao == null || !ehAdmin(autenticacao)) {
            if (novo.getFuncao() == null || novo.getFuncao().isBlank()) {
                novo.setFuncao(service.semUsuarios() ? "ADMIN" : "USER");
            }
        }

        Usuario salvo = service.salvar(novo);
        URI local = URI.create("/api/usuarios/" + salvo.getId());
        return ResponseEntity.created(local).body(UsuarioResponseDTO.de(salvo));
    }

    private boolean ehAdmin(Authentication autenticacao) {
        if (autenticacao == null || autenticacao.getAuthorities() == null) {
            return false;
        }

        return autenticacao.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ADMIN".equals(authority.getAuthority()));
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
