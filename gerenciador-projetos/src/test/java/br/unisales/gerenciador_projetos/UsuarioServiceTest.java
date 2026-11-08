package br.unisales.gerenciador_projetos;

import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.repository.UsuarioRepository;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    @DisplayName("codifica a senha antes de salvar o usuário")
    void salvarCodificaSenha() {
        Usuario usuario = new Usuario();
        usuario.setSenha("senha-em-texto");
        when(passwordEncoder.encode("senha-em-texto")).thenReturn("senha-codificada");
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.salvar(usuario);

        assertSame(usuario, resultado);
        assertEquals("senha-codificada", usuario.getSenha());
        verify(passwordEncoder).encode("senha-em-texto");
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("codifica a nova senha ao atualizar um usuário")
    void atualizarCodificaSenha() {
        Usuario existente = new Usuario();
        existente.setId(3L);
        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setNome("Bia");
        dadosAtualizados.setLogin("bia");
        dadosAtualizados.setSenha("nova-senha");
        dadosAtualizados.setFuncao("ADMIN");

        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nova-senha")).thenReturn("nova-senha-codificada");
        when(repository.save(existente)).thenReturn(existente);

        Usuario resultado = service.atualizar(3L, dadosAtualizados);

        assertSame(existente, resultado);
        assertEquals("Bia", existente.getNome());
        assertEquals("bia", existente.getLogin());
        assertEquals("nova-senha-codificada", existente.getSenha());
        assertEquals("ADMIN", existente.getFuncao());
        verify(repository).findById(3L);
        verify(passwordEncoder).encode("nova-senha");
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("lança exceção ao buscar usuário inexistente")
    void buscarPorIdInexistente() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> service.buscarPorId(3L));

        assertEquals("Usuário não encontrado", erro.getMessage());
        verify(repository).findById(3L);
    }
}
