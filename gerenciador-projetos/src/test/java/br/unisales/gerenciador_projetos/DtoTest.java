package br.unisales.gerenciador_projetos;

import br.unisales.gerenciador_projetos.entity.*;
import br.unisales.gerenciador_projetos.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @Nested
    @DisplayName("Usuário")
    class UsuarioDtos {

        @Test
        @DisplayName("UsuarioResponseDTO.de não expõe a senha")
        void responseNaoVazaSenha() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setNome("Filipe");
            usuario.setLogin("filipe");
            usuario.setSenha("senhaSuperSecreta");
            usuario.setFuncao("DEV");

            UsuarioResponseDTO dto = UsuarioResponseDTO.de(usuario);

            assertEquals("Filipe", dto.nome());
            assertEquals("filipe", dto.login());
            assertEquals("DEV", dto.funcao());
            assertFalse(dto.toString().contains("senhaSuperSecreta"),
                    "o record não deve ter nenhum campo de senha");
        }

        @Test
        @DisplayName("UsuarioResumoDTO.de traz só id, nome e login")
        void resumo() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setNome("Filipe");
            usuario.setLogin("filipe");

            UsuarioResumoDTO dto = UsuarioResumoDTO.de(usuario);

            assertEquals(1L, dto.id());
            assertEquals("filipe", dto.login());
        }

        @Test
        @DisplayName("de(null) devolve null em vez de lançar exceção")
        void conversaoDeNuloEhSegura() {
            assertNull(UsuarioResponseDTO.de(null));
            assertNull(UsuarioResumoDTO.de(null));
        }

        @Test
        @DisplayName("UsuarioRequestDTO.paraEntidade copia todos os campos")
        void requestParaEntidade() {
            var dto = new UsuarioRequestDTO("Filipe", "filipe", "senha123", "DEV");
            Usuario usuario = dto.paraEntidade();

            assertNull(usuario.getId(), "o id deve vir do banco, não do cliente");
            assertEquals("Filipe", usuario.getNome());
            assertEquals("filipe", usuario.getLogin());
            assertEquals("senha123", usuario.getSenha());
            assertEquals("DEV", usuario.getFuncao());
        }

        @Test
        @DisplayName("Usuário válido não gera violações")
        void validoNaoGeraViolacoes() {
            var dto = new UsuarioRequestDTO("Filipe", "filipe", "senha123", "DEV");
            assertTrue(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("Senha com menos de 6 caracteres é rejeitada")
        void senhaCurta() {
            var dto = new UsuarioRequestDTO("Filipe", "filipe", "123", "DEV");
            Set<ConstraintViolation<UsuarioRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("senha", violacoes.iterator().next().getPropertyPath().toString());
        }

        @Test
        @DisplayName("Nome e login em branco geram duas violações")
        void camposObrigatoriosEmBranco() {
            var dto = new UsuarioRequestDTO("", "  ", "senha123", null);
            assertEquals(2, validator.validate(dto).size());
        }

        @Test
        @DisplayName("Login com 1 caractere viola o tamanho mínimo")
        void loginMuitoCurto() {
            var dto = new UsuarioRequestDTO("Filipe", "f", "senha123", null);
            Set<ConstraintViolation<UsuarioRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("login", violacoes.iterator().next().getPropertyPath().toString());
        }
    }

    @Nested
    @DisplayName("Projeto")
    class ProjetoDtos {

        @Test
        @DisplayName("ProjetoResponseDTO.de copia todos os campos")
        void response() {
            Projeto projeto = new Projeto();
            projeto.setIdProjeto(1L);
            projeto.setNome("Sistema Acadêmico");
            projeto.setSupervisor("Maria");
            projeto.setDescricao("Gestão de matrículas");
            projeto.setDataInicio(LocalDate.of(2026, 1, 1));
            projeto.setDataFim(LocalDate.of(2026, 6, 30));
            projeto.setStatus("EM_ANDAMENTO");

            ProjetoResponseDTO dto = ProjetoResponseDTO.de(projeto);

            assertEquals("Sistema Acadêmico", dto.nome());
            assertEquals("Maria", dto.supervisor());
            assertEquals(LocalDate.of(2026, 6, 30), dto.dataFim());
            assertEquals("EM_ANDAMENTO", dto.status());
        }

        @Test
        @DisplayName("ProjetoResumoDTO.de traz só id, nome e status")
        void resumo() {
            Projeto projeto = new Projeto();
            projeto.setIdProjeto(1L);
            projeto.setNome("Sistema Acadêmico");
            projeto.setStatus("EM_ANDAMENTO");

            ProjetoResumoDTO dto = ProjetoResumoDTO.de(projeto);

            assertEquals(1L, dto.idProjeto());
            assertEquals("EM_ANDAMENTO", dto.status());
        }

        @Test
        @DisplayName("ProjetoRequestDTO.paraEntidade não define o id")
        void requestParaEntidade() {
            var dto = new ProjetoRequestDTO(
                    "Novo projeto", "Maria", "descrição",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "PLANEJADO");

            Projeto projeto = dto.paraEntidade();

            assertNull(projeto.getIdProjeto());
            assertEquals("Novo projeto", projeto.getNome());
            assertEquals(LocalDate.of(2026, 1, 1), projeto.getDataInicio());
        }

        @Test
        @DisplayName("Nome em branco é rejeitado")
        void nomeEmBranco() {
            var dto = new ProjetoRequestDTO("   ", null, null, null, null, null);
            Set<ConstraintViolation<ProjetoRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("nome", violacoes.iterator().next().getPropertyPath().toString());
        }

        @Test
        @DisplayName("Projeto só com nome já é válido (demais campos são opcionais)")
        void apenasNomeEhValido() {
            var dto = new ProjetoRequestDTO("Sistema Acadêmico", null, null, null, null, null);
            assertTrue(validator.validate(dto).isEmpty());
        }
    }

    @Nested
    @DisplayName("Tarefa")
    class TarefaDtos {

        @Test
        @DisplayName("TarefaResponseDTO aninha resumos e não entra em recursão")
        void responseUsaResumos() {
            Projeto projeto = new Projeto();
            projeto.setIdProjeto(1L);
            projeto.setNome("Sistema Acadêmico");

            Usuario responsavel = new Usuario();
            responsavel.setId(7L);
            responsavel.setNome("Filipe");
            responsavel.setLogin("filipe");

            Tarefa tarefa = new Tarefa();
            tarefa.setIdTarefa(10L);
            tarefa.setTitulo("Implementar login");
            tarefa.setPrazo(LocalDate.of(2026, 3, 15));
            tarefa.setProjeto(projeto);
            tarefa.setResponsavel(responsavel);
            // ciclo que quebraria a serialização se devolvêssemos a entidade
            projeto.setTarefas(List.of(tarefa));

            TarefaResponseDTO dto = TarefaResponseDTO.de(tarefa);

            assertEquals("Sistema Acadêmico", dto.projeto().nome());
            assertEquals("filipe", dto.responsavel().login());
            assertInstanceOf(ProjetoResumoDTO.class, dto.projeto());
        }

        @Test
        @DisplayName("Relacionamentos nulos viram null em vez de NullPointerException")
        void relacionamentoNuloNaoQuebra() {
            Tarefa tarefa = new Tarefa();
            tarefa.setIdTarefa(11L);
            tarefa.setTitulo("Tarefa solta");

            TarefaResponseDTO dto = assertDoesNotThrow(() -> TarefaResponseDTO.de(tarefa));

            assertNull(dto.projeto());
            assertNull(dto.responsavel());
        }

        @Test
        @DisplayName("TarefaRequestDTO.paraEntidade usa o projeto e o responsável já resolvidos")
        void requestParaEntidade() {
            Projeto projeto = new Projeto();
            projeto.setIdProjeto(1L);
            Usuario responsavel = new Usuario();
            responsavel.setId(7L);

            var dto = new TarefaRequestDTO(
                    "Implementar login", "desc", LocalDate.now(), LocalDate.now().plusDays(5),
                    "ALTA", "PENDENTE", 1L, 7L);

            Tarefa tarefa = dto.paraEntidade(projeto, responsavel);

            assertSame(projeto, tarefa.getProjeto());
            assertSame(responsavel, tarefa.getResponsavel());
            assertEquals("Implementar login", tarefa.getTitulo());
        }

        @Test
        @DisplayName("Título em branco é rejeitado")
        void tituloEmBranco() {
            var dto = new TarefaRequestDTO("  ", null, null, null, null, null, 1L, null);
            Set<ConstraintViolation<TarefaRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("titulo", violacoes.iterator().next().getPropertyPath().toString());
        }

        @Test
        @DisplayName("Tarefa sem idProjeto é rejeitada")
        void semProjeto() {
            var dto = new TarefaRequestDTO("Título", null, null, null, null, null, null, null);
            Set<ConstraintViolation<TarefaRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("idProjeto", violacoes.iterator().next().getPropertyPath().toString());
        }

        @Test
        @DisplayName("Tarefa sem responsável é válida (o campo é opcional)")
        void semResponsavelEhValido() {
            var dto = new TarefaRequestDTO("Título", null, null, null, null, null, 1L, null);
            assertTrue(validator.validate(dto).isEmpty());
        }
    }

    @Nested
    @DisplayName("Equipe")
    class EquipeDtos {

        @Test
        @DisplayName("EquipeResponseDTO aninha os resumos de projeto e usuário")
        void response() {
            Projeto projeto = new Projeto();
            projeto.setIdProjeto(1L);
            projeto.setNome("Sistema Acadêmico");

            Usuario usuario = new Usuario();
            usuario.setId(7L);
            usuario.setNome("Filipe");

            Equipe equipe = new Equipe();
            equipe.setIdEquipe(3L);
            equipe.setNome("Squad Backend");
            equipe.setProjeto(projeto);
            equipe.setUsuario(usuario);

            EquipeResponseDTO dto = EquipeResponseDTO.de(equipe);

            assertEquals("Squad Backend", dto.nome());
            assertEquals("Sistema Acadêmico", dto.projeto().nome());
            assertEquals("Filipe", dto.usuario().nome());
        }

        @Test
        @DisplayName("EquipeResumoDTO.de traz só id e nome")
        void resumo() {
            Equipe equipe = new Equipe();
            equipe.setIdEquipe(3L);
            equipe.setNome("Squad Backend");

            EquipeResumoDTO dto = EquipeResumoDTO.de(equipe);

            assertEquals(3L, dto.idEquipe());
            assertEquals("Squad Backend", dto.nome());
        }

        @Test
        @DisplayName("EquipeRequestDTO.paraEntidade aceita projeto e usuário nulos")
        void requestComRelacionamentosNulos() {
            var dto = new EquipeRequestDTO("Squad Backend", "descrição", null, null);
            Equipe equipe = assertDoesNotThrow(() -> dto.paraEntidade(null, null));

            assertEquals("Squad Backend", equipe.getNome());
            assertNull(equipe.getProjeto());
        }

        @Test
        @DisplayName("Nome em branco é rejeitado")
        void nomeEmBranco() {
            var dto = new EquipeRequestDTO("", null, 1L, 7L);
            assertEquals(1, validator.validate(dto).size());
        }
    }

    @Nested
    @DisplayName("Anexo")
    class AnexoDtos {

        @Test
        @DisplayName("AnexoResponseDTO.de expõe o id da tarefa sem aninhar a tarefa inteira")
        void response() {
            Tarefa tarefa = new Tarefa();
            tarefa.setIdTarefa(10L);

            Usuario usuario = new Usuario();
            usuario.setId(7L);
            usuario.setNome("Filipe");

            Anexo anexo = new Anexo();
            anexo.setId(5L);
            anexo.setNomeArquivo("diagrama.png");
            anexo.setUrlArquivo("https://arquivos.exemplo/diagrama.png");
            anexo.setTipoArquivo("image/png");
            anexo.setTarefa(tarefa);
            anexo.setUsuario(usuario);

            AnexoResponseDTO dto = AnexoResponseDTO.de(anexo);

            assertEquals("diagrama.png", dto.nomeArquivo());
            assertEquals(10L, dto.idTarefa());
            assertEquals("Filipe", dto.usuario().nome());
        }

        @Test
        @DisplayName("idTarefa vem null quando a tarefa do anexo é null")
        void tarefaNulaNaoQuebra() {
            Anexo anexo = new Anexo();
            anexo.setId(5L);
            anexo.setNomeArquivo("solto.png");

            AnexoResponseDTO dto = assertDoesNotThrow(() -> AnexoResponseDTO.de(anexo));

            assertNull(dto.idTarefa());
            assertNull(dto.usuario());
        }

        @Test
        @DisplayName("AnexoRequestDTO.paraEntidade usa a tarefa já resolvida")
        void requestParaEntidade() {
            Tarefa tarefa = new Tarefa();
            tarefa.setIdTarefa(10L);

            var dto = new AnexoRequestDTO(10L, "diagrama.png",
                    "https://arquivos.exemplo/diagrama.png", "image/png", null);

            Anexo anexo = dto.paraEntidade(tarefa, null);

            assertSame(tarefa, anexo.getTarefa());
            assertEquals("diagrama.png", anexo.getNomeArquivo());
        }

        @Test
        @DisplayName("Anexo sem idTarefa é rejeitado")
        void semTarefa() {
            var dto = new AnexoRequestDTO(null, "diagrama.png",
                    "https://arquivos.exemplo/diagrama.png", null, null);
            Set<ConstraintViolation<AnexoRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("idTarefa", violacoes.iterator().next().getPropertyPath().toString());
        }

        @Test
        @DisplayName("Anexo sem urlArquivo é rejeitado")
        void semUrl() {
            var dto = new AnexoRequestDTO(10L, "diagrama.png", "  ", null, null);
            Set<ConstraintViolation<AnexoRequestDTO>> violacoes = validator.validate(dto);

            assertEquals(1, violacoes.size());
            assertEquals("urlArquivo", violacoes.iterator().next().getPropertyPath().toString());
        }
    }

    @Nested
    @DisplayName("PageResponseDTO")
    class PageResponseDtoTest {

        @Test
        @DisplayName("copia os metadados do Page e converte o conteúdo")
        void conversao() {
            Projeto a = new Projeto();
            a.setIdProjeto(1L);
            a.setNome("Projeto A");

            Projeto b = new Projeto();
            b.setIdProjeto(2L);
            b.setNome("Projeto B");

            Page<Projeto> page = new PageImpl<>(List.of(a, b), PageRequest.of(0, 2), 5);

            PageResponseDTO<ProjetoResumoDTO> resposta =
                    PageResponseDTO.de(page, ProjetoResumoDTO::de);

            assertEquals(2, resposta.conteudo().size());
            assertEquals("Projeto A", resposta.conteudo().get(0).nome());
            assertEquals(0, resposta.pagina());
            assertEquals(5, resposta.totalElementos());
            assertEquals(3, resposta.totalPaginas());
            assertTrue(resposta.primeira());
            assertFalse(resposta.ultima());
        }

        @Test
        @DisplayName("última página vem marcada corretamente")
        void ultimaPagina() {
            Projeto a = new Projeto();
            a.setIdProjeto(5L);
            a.setNome("Projeto E");

            Page<Projeto> page = new PageImpl<>(List.of(a), PageRequest.of(2, 2), 5);

            PageResponseDTO<ProjetoResumoDTO> resposta =
                    PageResponseDTO.de(page, ProjetoResumoDTO::de);

            assertTrue(resposta.ultima());
            assertFalse(resposta.primeira());
        }
    }
}
