package br.unisales.gerenciador_projetos;

import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.repository.ProjetoRepository;
import br.unisales.gerenciador_projetos.service.ProjetoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository repository;

    @InjectMocks
    private ProjetoService service;

    @Test
    @DisplayName("lista projetos usando o repositório")
    void listarTodos() {
        List<Projeto> projetos = List.of(new Projeto(), new Projeto());
        when(repository.findAll()).thenReturn(projetos);

        List<Projeto> resultado = service.listarTodos();

        assertSame(projetos, resultado);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("busca projeto pelo identificador")
    void buscarPorIdExistente() {
        Projeto projeto = new Projeto();
        when(repository.findById(7L)).thenReturn(Optional.of(projeto));

        Projeto resultado = service.buscarPorId(7L);

        assertSame(projeto, resultado);
        verify(repository).findById(7L);
    }

    @Test
    @DisplayName("lança exceção quando o projeto não existe")
    void buscarPorIdInexistente() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> service.buscarPorId(7L));

        assertEquals("Projeto não encontrado", erro.getMessage());
        verify(repository).findById(7L);
    }

    @Test
    @DisplayName("salva o projeto recebido")
    void salvar() {
        Projeto projeto = new Projeto();
        when(repository.save(projeto)).thenReturn(projeto);

        Projeto resultado = service.salvar(projeto);

        assertSame(projeto, resultado);
        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("atualiza os campos do projeto existente")
    void atualizar() {
        Projeto existente = new Projeto();
        existente.setIdProjeto(7L);
        Projeto dadosAtualizados = new Projeto();
        dadosAtualizados.setNome("Projeto novo");
        dadosAtualizados.setSupervisor("Ana");
        dadosAtualizados.setDescricao("Descrição atualizada");
        dadosAtualizados.setDataInicio(LocalDate.of(2026, 1, 10));
        dadosAtualizados.setDataFim(LocalDate.of(2026, 6, 30));
        dadosAtualizados.setStatus("Em andamento");

        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Projeto resultado = service.atualizar(7L, dadosAtualizados);

        assertSame(existente, resultado);
        assertEquals("Projeto novo", existente.getNome());
        assertEquals("Ana", existente.getSupervisor());
        assertEquals("Descrição atualizada", existente.getDescricao());
        assertEquals(LocalDate.of(2026, 1, 10), existente.getDataInicio());
        assertEquals(LocalDate.of(2026, 6, 30), existente.getDataFim());
        assertEquals("Em andamento", existente.getStatus());
        verify(repository).findById(7L);
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("remove do repositório o projeto encontrado")
    void excluir() {
        Projeto projeto = new Projeto();
        when(repository.findById(7L)).thenReturn(Optional.of(projeto));

        service.excluir(7L);

        verify(repository).findById(7L);
        verify(repository).delete(projeto);
    }
}
