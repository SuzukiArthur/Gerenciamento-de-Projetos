package br.unisales.gerenciador_projetos;

import br.unisales.gerenciador_projetos.controller.AnexoController;
import br.unisales.gerenciador_projetos.controller.EquipeController;
import br.unisales.gerenciador_projetos.controller.ProjetoController;
import br.unisales.gerenciador_projetos.controller.TarefaController;
import br.unisales.gerenciador_projetos.controller.UsuarioController;
import br.unisales.gerenciador_projetos.entity.Anexo;
import br.unisales.gerenciador_projetos.entity.Equipe;
import br.unisales.gerenciador_projetos.entity.Projeto;
import br.unisales.gerenciador_projetos.entity.Tarefa;
import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.exception.GlobalExceptionHandler;
import br.unisales.gerenciador_projetos.exception.ResourceNotFoundException;
import br.unisales.gerenciador_projetos.repository.EquipeRepository;
import br.unisales.gerenciador_projetos.repository.ProjetoRepository;
import br.unisales.gerenciador_projetos.service.AnexoService;
import br.unisales.gerenciador_projetos.service.ProjetoService;
import br.unisales.gerenciador_projetos.service.TarefaService;
import br.unisales.gerenciador_projetos.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllersTest {

    // Utilitários compartilhados
    private static MockMvc criarMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static MockHttpServletRequestBuilder comJson(MockHttpServletRequestBuilder requisicao, String corpo) {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private static Usuario usuario(Long id, String nome, String login) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setSenha("$2a$10$hashqualquerdesenha");
        usuario.setFuncao("DEV");
        return usuario;
    }

    private static Projeto projeto(Long id, String nome, String status) {
        Projeto projeto = new Projeto();
        projeto.setIdProjeto(id);
        projeto.setNome(nome);
        projeto.setSupervisor("Arthur");
        projeto.setDescricao("Descrição do projeto");
        projeto.setStatus(status);
        return projeto;
    }

    private static Tarefa tarefa(Long id, String titulo, Projeto projeto, Usuario responsavel) {
        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(id);
        tarefa.setTitulo(titulo);
        tarefa.setPrioridade("ALTA");
        tarefa.setStatus("PENDENTE");
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(responsavel);
        return tarefa;
    }

    private static Anexo anexo(Long id, Tarefa tarefa, Usuario usuario) {
        Anexo anexo = new Anexo();
        anexo.setId(id);
        anexo.setTarefa(tarefa);
        anexo.setNomeArquivo("wireframe.pdf");
        anexo.setUrlArquivo("https://exemplo.com/wireframe.pdf");
        anexo.setTipoArquivo("application/pdf");
        anexo.setUsuario(usuario);
        return anexo;
    }

    private static Equipe equipe(Long id, String nome, Projeto projeto, Usuario usuario) {
        Equipe equipe = new Equipe();
        equipe.setIdEquipe(id);
        equipe.setNome(nome);
        equipe.setDescricao("Descrição da equipe");
        equipe.setProjeto(projeto);
        equipe.setUsuario(usuario);
        return equipe;
    }

    // Usuários
    @Nested
    @DisplayName("UsuarioController")
    class Usuarios {

        private UsuarioService service;
        private MockMvc mvc;

        @BeforeEach
        void preparar() {
            service = mock(UsuarioService.class);
            mvc = criarMvc(new UsuarioController(service));
        }

        @Test
        @DisplayName("GET /api/usuarios devolve o resumo de cada usuário, sem senha")
        void listar() throws Exception {
            when(service.listarTodos()).thenReturn(List.of(
                    usuario(1L, "Ana", "ana"),
                    usuario(2L, "Bia", "bia")));

            mvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].login").value("ana"))
                    .andExpect(jsonPath("$[1].nome").value("Bia"))
                    .andExpect(jsonPath("$[0].senha").doesNotExist())
                    .andExpect(content().string(not(containsString("hashqualquerdesenha"))));
        }

        @Test
        @DisplayName("GET /api/usuarios sem registros devolve lista vazia")
        void listarVazio() throws Exception {
            when(service.listarTodos()).thenReturn(List.of());

            mvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} devolve o usuário sem expor a senha")
        void buscarPorId() throws Exception {
            when(service.buscarPorId(1L)).thenReturn(usuario(1L, "Ana", "ana"));

            mvc.perform(get("/api/usuarios/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Ana"))
                    .andExpect(jsonPath("$.funcao").value("DEV"))
                    .andExpect(jsonPath("$.senha").doesNotExist())
                    .andExpect(content().string(not(containsString("hashqualquerdesenha"))));
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} inexistente vira 404")
        void buscarInexistente() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Usuario 99 nao encontrado"));

            mvc.perform(get("/api/usuarios/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/usuarios cria, devolve 201 com Location e limpa espaços do nome")
        void criar() throws Exception {
            when(service.salvar(any(Usuario.class))).thenAnswer(inv -> {
                Usuario salvo = inv.getArgument(0);
                salvo.setId(7L);
                return salvo;
            });

            mvc.perform(comJson(post("/api/usuarios"), """
                            {"nome":"  Filipe ","login":"filipe","senha":"senha123","funcao":"DEV"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/usuarios/7"))
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.nome").value("Filipe"))
                    .andExpect(jsonPath("$.senha").doesNotExist());

            ArgumentCaptor<Usuario> enviado = ArgumentCaptor.forClass(Usuario.class);
            verify(service).salvar(enviado.capture());
            assertEquals("Filipe", enviado.getValue().getNome());
            assertEquals("filipe", enviado.getValue().getLogin());
            assertEquals("senha123", enviado.getValue().getSenha());
        }

        @Test
        @DisplayName("POST /api/usuarios sem campos obrigatórios devolve 400 por campo")
        void criarSemObrigatorios() throws Exception {
            mvc.perform(comJson(post("/api/usuarios"), "{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome é obrigatório"))
                    .andExpect(jsonPath("$.login").value("O login é obrigatório"))
                    .andExpect(jsonPath("$.senha").value("A senha é obrigatória"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("POST /api/usuarios com login e senha curtos devolve 400")
        void criarComValoresCurtos() throws Exception {
            mvc.perform(comJson(post("/api/usuarios"), """
                            {"nome":"   ","login":"ab","senha":"123"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome é obrigatório"))
                    .andExpect(jsonPath("$.login").value("O login deve ter entre 3 e 50 caracteres"))
                    .andExpect(jsonPath("$.senha").value("A senha deve ter no mínimo 6 caracteres"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} atualiza e devolve 200")
        void atualizar() throws Exception {
            when(service.atualizar(eq(3L), any(Usuario.class))).thenAnswer(inv -> {
                Usuario atualizado = inv.getArgument(1);
                atualizado.setId(3L);
                return atualizado;
            });

            mvc.perform(comJson(put("/api/usuarios/3"), """
                            {"nome":"Ana Maria","login":"ana","senha":"novaSenha1","funcao":"ADMIN"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.nome").value("Ana Maria"))
                    .andExpect(jsonPath("$.funcao").value("ADMIN"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} com corpo inválido devolve 400 e não chama o service")
        void atualizarInvalido() throws Exception {
            mvc.perform(comJson(put("/api/usuarios/3"), """
                            {"nome":"Ana","login":"ana","senha":"123"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.senha").value("A senha deve ter no mínimo 6 caracteres"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} inexistente vira 404")
        void atualizarInexistente() throws Exception {
            when(service.atualizar(eq(99L), any(Usuario.class)))
                    .thenThrow(new ResourceNotFoundException("Usuario 99 nao encontrado"));

            mvc.perform(comJson(put("/api/usuarios/99"), """
                            {"nome":"Ana","login":"ana","senha":"senha123"}
                            """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} devolve 204")
        void excluir() throws Exception {
            mvc.perform(delete("/api/usuarios/3"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(3L);
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} inexistente vira 404")
        void excluirInexistente() throws Exception {
            doThrow(new ResourceNotFoundException("Usuario 99 nao encontrado")).when(service).excluir(99L);

            mvc.perform(delete("/api/usuarios/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // Projetos
    @Nested
    @DisplayName("ProjetoController")
    class Projetos {

        private ProjetoService service;
        private TarefaService tarefaService;
        private ProjetoRepository repository;
        private MockMvc mvc;

        @BeforeEach
        void preparar() {
            service = mock(ProjetoService.class);
            tarefaService = mock(TarefaService.class);
            repository = mock(ProjetoRepository.class);
            mvc = criarMvc(new ProjetoController(service, tarefaService, repository));
        }

        @Test
        @DisplayName("GET /api/projetos devolve só id, nome e status")
        void listar() throws Exception {
            when(service.listarTodos()).thenReturn(List.of(
                    projeto(1L, "Site novo", "EM_ANDAMENTO"),
                    projeto(2L, "App mobile", "PLANEJAMENTO")));

            mvc.perform(get("/api/projetos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].idProjeto").value(1))
                    .andExpect(jsonPath("$[0].nome").value("Site novo"))
                    .andExpect(jsonPath("$[1].status").value("PLANEJAMENTO"))
                    .andExpect(jsonPath("$[0].supervisor").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/projetos/paginado usa página 0, tamanho 10 e ordena por idProjeto")
        void paginadoComPadroes() throws Exception {
            Page<Projeto> pagina = new PageImpl<>(
                    List.of(projeto(1L, "Site novo", "EM_ANDAMENTO")), PageRequest.of(0, 10), 1);
            when(repository.findAll(any(Pageable.class))).thenReturn(pagina);

            mvc.perform(get("/api/projetos/paginado"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conteudo", hasSize(1)))
                    .andExpect(jsonPath("$.conteudo[0].nome").value("Site novo"))
                    .andExpect(jsonPath("$.pagina").value(0))
                    .andExpect(jsonPath("$.tamanho").value(10))
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.totalPaginas").value(1))
                    .andExpect(jsonPath("$.primeira").value(true))
                    .andExpect(jsonPath("$.ultima").value(true));

            ArgumentCaptor<Pageable> pedido = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(pedido.capture());
            assertEquals(0, pedido.getValue().getPageNumber());
            assertEquals(10, pedido.getValue().getPageSize());
            assertNotNull(pedido.getValue().getSort().getOrderFor("idProjeto"));
        }

        @Test
        @DisplayName("GET /api/projetos/paginado repassa pagina, tamanho e ordenarPor ao repository")
        void paginadoComParametros() throws Exception {
            Page<Projeto> pagina = new PageImpl<>(
                    List.of(projeto(11L, "K", "ATIVO"), projeto(12L, "L", "ATIVO")),
                    PageRequest.of(2, 5), 12);
            when(repository.findAll(any(Pageable.class))).thenReturn(pagina);

            mvc.perform(get("/api/projetos/paginado?pagina=2&tamanho=5&ordenarPor=nome"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conteudo", hasSize(2)))
                    .andExpect(jsonPath("$.pagina").value(2))
                    .andExpect(jsonPath("$.tamanho").value(5))
                    .andExpect(jsonPath("$.totalElementos").value(12))
                    .andExpect(jsonPath("$.totalPaginas").value(3))
                    .andExpect(jsonPath("$.primeira").value(false))
                    .andExpect(jsonPath("$.ultima").value(true));

            ArgumentCaptor<Pageable> pedido = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(pedido.capture());
            assertEquals(2, pedido.getValue().getPageNumber());
            assertEquals(5, pedido.getValue().getPageSize());
            assertNotNull(pedido.getValue().getSort().getOrderFor("nome"));
        }

        @Test
        @DisplayName("GET /api/projetos/{id} devolve o projeto completo")
        void buscarPorId() throws Exception {
            when(service.buscarPorId(1L)).thenReturn(projeto(1L, "Site novo", "EM_ANDAMENTO"));

            mvc.perform(get("/api/projetos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idProjeto").value(1))
                    .andExpect(jsonPath("$.nome").value("Site novo"))
                    .andExpect(jsonPath("$.supervisor").value("Arthur"))
                    .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        }

        @Test
        @DisplayName("GET /api/projetos/{id} inexistente vira 404")
        void buscarInexistente() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(get("/api/projetos/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/projetos/{id}/tarefas lista as tarefas do projeto")
        void listarTarefas() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(service.buscarPorId(1L)).thenReturn(projeto);
            when(tarefaService.listarPorProjeto(1L)).thenReturn(List.of(
                    tarefa(10L, "Criar login", projeto, null)));

            mvc.perform(get("/api/projetos/1/tarefas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].titulo").value("Criar login"))
                    .andExpect(jsonPath("$[0].projeto.idProjeto").value(1))
                    .andExpect(jsonPath("$[0].responsavel").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/projetos/{id}/tarefas de projeto inexistente vira 404 e não consulta tarefas")
        void listarTarefasDeProjetoInexistente() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(get("/api/projetos/99/tarefas"))
                    .andExpect(status().isNotFound());

            verifyNoInteractions(tarefaService);
        }

        @Test
        @DisplayName("POST /api/projetos cria, devolve 201 com Location e converte as datas")
        void criar() throws Exception {
            when(service.salvar(any(Projeto.class))).thenAnswer(inv -> {
                Projeto salvo = inv.getArgument(0);
                salvo.setIdProjeto(5L);
                return salvo;
            });

            mvc.perform(comJson(post("/api/projetos"), """
                            {"nome":"Novo","supervisor":"Arthur","dataInicio":"2026-03-01",
                             "dataFim":"2026-08-30","status":"EM_ANDAMENTO"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/projetos/5"))
                    .andExpect(jsonPath("$.idProjeto").value(5))
                    .andExpect(jsonPath("$.nome").value("Novo"));

            ArgumentCaptor<Projeto> enviado = ArgumentCaptor.forClass(Projeto.class);
            verify(service).salvar(enviado.capture());
            assertEquals(LocalDate.of(2026, 3, 1), enviado.getValue().getDataInicio());
            assertEquals(LocalDate.of(2026, 8, 30), enviado.getValue().getDataFim());
            assertEquals("EM_ANDAMENTO", enviado.getValue().getStatus());
        }

        @Test
        @DisplayName("POST /api/projetos sem nome devolve 400")
        void criarSemNome() throws Exception {
            mvc.perform(comJson(post("/api/projetos"), """
                            {"supervisor":"Arthur"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome do projeto é obrigatório"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("POST /api/projetos com nome acima de 255 caracteres devolve 400")
        void criarComNomeGrande() throws Exception {
            String corpo = "{\"nome\":\"" + "a".repeat(256) + "\"}";

            mvc.perform(comJson(post("/api/projetos"), corpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome deve ter no máximo 255 caracteres"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("PUT /api/projetos/{id} atualiza e devolve 200")
        void atualizar() throws Exception {
            when(service.atualizar(eq(5L), any(Projeto.class))).thenAnswer(inv -> {
                Projeto atualizado = inv.getArgument(1);
                atualizado.setIdProjeto(5L);
                return atualizado;
            });

            mvc.perform(comJson(put("/api/projetos/5"), """
                            {"nome":"Renomeado","status":"CONCLUIDO"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idProjeto").value(5))
                    .andExpect(jsonPath("$.nome").value("Renomeado"))
                    .andExpect(jsonPath("$.status").value("CONCLUIDO"));
        }

        @Test
        @DisplayName("PUT /api/projetos/{id} com corpo inválido devolve 400 e não chama o service")
        void atualizarInvalido() throws Exception {
            mvc.perform(comJson(put("/api/projetos/5"), "{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("PUT /api/projetos/{id} inexistente vira 404")
        void atualizarInexistente() throws Exception {
            when(service.atualizar(eq(99L), any(Projeto.class)))
                    .thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(comJson(put("/api/projetos/99"), """
                            {"nome":"Renomeado"}
                            """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/projetos com data em formato inválido devolve 400 e não chama o service")
        void criarComDataInvalida() throws Exception {
            mvc.perform(comJson(post("/api/projetos"), """
                            {"nome":"Novo","dataInicio":"01/03/2026"}
                            """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("DELETE /api/projetos/{id} devolve 204")
        void excluir() throws Exception {
            mvc.perform(delete("/api/projetos/5"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(5L);
        }

        @Test
        @DisplayName("DELETE /api/projetos/{id} inexistente vira 404")
        void excluirInexistente() throws Exception {
            doThrow(new ResourceNotFoundException("Projeto 99 nao encontrado")).when(service).excluir(99L);

            mvc.perform(delete("/api/projetos/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // Tarefas
    @Nested
    @DisplayName("TarefaController")
    class Tarefas {

        private TarefaService service;
        private ProjetoService projetoService;
        private UsuarioService usuarioService;
        private MockMvc mvc;

        @BeforeEach
        void preparar() {
            service = mock(TarefaService.class);
            projetoService = mock(ProjetoService.class);
            usuarioService = mock(UsuarioService.class);
            mvc = criarMvc(new TarefaController(service, projetoService, usuarioService));
        }

        @Test
        @DisplayName("GET /api/tarefas sem filtro lista todas")
        void listarTodas() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(service.listarTodos()).thenReturn(List.of(
                    tarefa(1L, "Criar login", projeto, null),
                    tarefa(2L, "Criar home", projeto, null)));

            mvc.perform(get("/api/tarefas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[1].titulo").value("Criar home"));

            verify(service, never()).listarPorProjeto(anyLong());
            verify(service, never()).listarPorResponsavel(anyLong());
        }

        @Test
        @DisplayName("GET /api/tarefas?projetoId= filtra por projeto")
        void filtrarPorProjeto() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(service.listarPorProjeto(1L)).thenReturn(List.of(tarefa(1L, "Criar login", projeto, null)));

            mvc.perform(get("/api/tarefas?projetoId=1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].projeto.idProjeto").value(1));

            verify(service, never()).listarTodos();
        }

        @Test
        @DisplayName("GET /api/tarefas?responsavelId= filtra por responsável")
        void filtrarPorResponsavel() throws Exception {
            Usuario ana = usuario(3L, "Ana", "ana");
            when(service.listarPorResponsavel(3L)).thenReturn(List.of(
                    tarefa(1L, "Criar login", projeto(1L, "Site novo", "ATIVO"), ana)));

            mvc.perform(get("/api/tarefas?responsavelId=3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].responsavel.login").value("ana"));

            verify(service, never()).listarTodos();
        }

        @Test
        @DisplayName("GET /api/tarefas com projetoId e responsavelId considera só o projeto")
        void projetoTemPrioridadeSobreResponsavel() throws Exception {
            when(service.listarPorProjeto(1L)).thenReturn(List.of());

            mvc.perform(get("/api/tarefas?projetoId=1&responsavelId=3"))
                    .andExpect(status().isOk());

            verify(service).listarPorProjeto(1L);
            verify(service, never()).listarPorResponsavel(anyLong());
        }

        @Test
        @DisplayName("GET /api/tarefas/{id} devolve a tarefa com projeto e responsável resumidos")
        void buscarPorId() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            Usuario ana = usuario(3L, "Ana", "ana");
            when(service.buscarPorId(10L)).thenReturn(tarefa(10L, "Criar login", projeto, ana));

            mvc.perform(get("/api/tarefas/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idTarefa").value(10))
                    .andExpect(jsonPath("$.titulo").value("Criar login"))
                    .andExpect(jsonPath("$.prioridade").value("ALTA"))
                    .andExpect(jsonPath("$.projeto.nome").value("Site novo"))
                    .andExpect(jsonPath("$.responsavel.nome").value("Ana"))
                    .andExpect(jsonPath("$.responsavel.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/tarefas/{id} inexistente vira 404")
        void buscarInexistente() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Tarefa 99 nao encontrada"));

            mvc.perform(get("/api/tarefas/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/tarefas busca projeto e responsável, cria e devolve 201 com Location")
        void criar() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            Usuario ana = usuario(2L, "Ana", "ana");
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(usuarioService.buscarPorId(2L)).thenReturn(ana);
            when(service.salvar(any(Tarefa.class))).thenAnswer(inv -> {
                Tarefa salva = inv.getArgument(0);
                salva.setIdTarefa(20L);
                return salva;
            });

            mvc.perform(comJson(post("/api/tarefas"), """
                            {"titulo":"Criar login","prazo":"2026-03-15","prioridade":"ALTA",
                             "status":"PENDENTE","idProjeto":1,"idResponsavel":2}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/tarefas/20"))
                    .andExpect(jsonPath("$.idTarefa").value(20))
                    .andExpect(jsonPath("$.projeto.idProjeto").value(1))
                    .andExpect(jsonPath("$.responsavel.login").value("ana"));

            ArgumentCaptor<Tarefa> enviada = ArgumentCaptor.forClass(Tarefa.class);
            verify(service).salvar(enviada.capture());
            assertSame(projeto, enviada.getValue().getProjeto());
            assertSame(ana, enviada.getValue().getResponsavel());
            assertEquals(LocalDate.of(2026, 3, 15), enviada.getValue().getPrazo());
        }

        @Test
        @DisplayName("POST /api/tarefas sem idResponsavel cria a tarefa sem consultar usuários")
        void criarSemResponsavel() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(service.salvar(any(Tarefa.class))).thenAnswer(inv -> {
                Tarefa salva = inv.getArgument(0);
                salva.setIdTarefa(21L);
                return salva;
            });

            mvc.perform(comJson(post("/api/tarefas"), """
                            {"titulo":"Criar home","idProjeto":1}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.responsavel").doesNotExist());

            verifyNoInteractions(usuarioService);

            ArgumentCaptor<Tarefa> enviada = ArgumentCaptor.forClass(Tarefa.class);
            verify(service).salvar(enviada.capture());
            assertNull(enviada.getValue().getResponsavel());
        }

        @Test
        @DisplayName("POST /api/tarefas sem título e sem projeto devolve 400")
        void criarSemObrigatorios() throws Exception {
            mvc.perform(comJson(post("/api/tarefas"), "{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.titulo").value("O título da tarefa é obrigatório"))
                    .andExpect(jsonPath("$.idProjeto").value("A tarefa precisa estar vinculada a um projeto"));

            verifyNoInteractions(service, projetoService, usuarioService);
        }

        @Test
        @DisplayName("POST /api/tarefas com projeto inexistente vira 404 e não salva")
        void criarComProjetoInexistente() throws Exception {
            when(projetoService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(comJson(post("/api/tarefas"), """
                            {"titulo":"Criar login","idProjeto":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).salvar(any(Tarefa.class));
        }

        @Test
        @DisplayName("PUT /api/tarefas/{id} atualiza e devolve 200")
        void atualizar() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(service.atualizar(eq(10L), any(Tarefa.class))).thenAnswer(inv -> {
                Tarefa atualizada = inv.getArgument(1);
                atualizada.setIdTarefa(10L);
                return atualizada;
            });

            mvc.perform(comJson(put("/api/tarefas/10"), """
                            {"titulo":"Criar login com OAuth","status":"EM_ANDAMENTO","idProjeto":1}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idTarefa").value(10))
                    .andExpect(jsonPath("$.titulo").value("Criar login com OAuth"))
                    .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        }

        @Test
        @DisplayName("PUT /api/tarefas/{id} com corpo inválido devolve 400 e não chama o service")
        void atualizarInvalido() throws Exception {
            mvc.perform(comJson(put("/api/tarefas/10"), """
                            {"titulo":"   ","idProjeto":1}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.titulo").value("O título da tarefa é obrigatório"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("POST /api/tarefas com responsável inexistente vira 404 e não salva")
        void criarComResponsavelInexistente() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto(1L, "Site novo", "EM_ANDAMENTO"));
            when(usuarioService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Usuario 99 nao encontrado"));

            mvc.perform(comJson(post("/api/tarefas"), """
                            {"titulo":"Criar login","idProjeto":1,"idResponsavel":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).salvar(any(Tarefa.class));
        }

        @Test
        @DisplayName("PUT /api/tarefas/{id} com projeto inexistente vira 404 e não atualiza")
        void atualizarComProjetoInexistente() throws Exception {
            when(projetoService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(comJson(put("/api/tarefas/10"), """
                            {"titulo":"Criar login","idProjeto":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).atualizar(anyLong(), any(Tarefa.class));
        }

        @Test
        @DisplayName("PUT /api/tarefas/{id} inexistente vira 404")
        void atualizarInexistente() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto(1L, "Site novo", "EM_ANDAMENTO"));
            when(service.atualizar(eq(99L), any(Tarefa.class)))
                    .thenThrow(new ResourceNotFoundException("Tarefa 99 nao encontrada"));

            mvc.perform(comJson(put("/api/tarefas/99"), """
                            {"titulo":"Criar login","idProjeto":1}
                            """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/tarefas/{id} inexistente vira 404")
        void excluirInexistente() throws Exception {
            doThrow(new ResourceNotFoundException("Tarefa 99 nao encontrada")).when(service).excluir(99L);

            mvc.perform(delete("/api/tarefas/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/tarefas/{id} devolve 204")
        void excluir() throws Exception {
            mvc.perform(delete("/api/tarefas/10"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(10L);
        }
    }

    // Equipes (usa o repository, sem service)
    @Nested
    @DisplayName("EquipeController")
    class Equipes {

        private EquipeRepository repository;
        private ProjetoService projetoService;
        private UsuarioService usuarioService;
        private MockMvc mvc;

        @BeforeEach
        void preparar() {
            repository = mock(EquipeRepository.class);
            projetoService = mock(ProjetoService.class);
            usuarioService = mock(UsuarioService.class);
            mvc = criarMvc(new EquipeController(repository, projetoService, usuarioService));
        }

        @Test
        @DisplayName("GET /api/equipes devolve só id e nome")
        void listar() throws Exception {
            when(repository.findAll()).thenReturn(List.of(
                    equipe(1L, "Backend", null, null),
                    equipe(2L, "Frontend", null, null)));

            mvc.perform(get("/api/equipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].idEquipe").value(1))
                    .andExpect(jsonPath("$[1].nome").value("Frontend"))
                    .andExpect(jsonPath("$[0].descricao").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/equipes/{id} devolve a equipe com projeto e usuário resumidos")
        void buscarPorId() throws Exception {
            Equipe equipe = equipe(1L, "Backend",
                    projeto(3L, "Site novo", "EM_ANDAMENTO"), usuario(4L, "Ana", "ana"));
            when(repository.findById(1L)).thenReturn(Optional.of(equipe));

            mvc.perform(get("/api/equipes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEquipe").value(1))
                    .andExpect(jsonPath("$.descricao").value("Descrição da equipe"))
                    .andExpect(jsonPath("$.projeto.nome").value("Site novo"))
                    .andExpect(jsonPath("$.usuario.login").value("ana"))
                    .andExpect(jsonPath("$.usuario.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/equipes/{id} inexistente devolve 404 com a mensagem")
        void buscarInexistente() throws Exception {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // só checa o trecho sem acento para não depender do charset do texto puro
            mvc.perform(get("/api/equipes/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Equipe 99")));
        }

        @Test
        @DisplayName("POST /api/equipes busca projeto e usuário, salva e devolve 201 com Location")
        void criar() throws Exception {
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            Usuario ana = usuario(2L, "Ana", "ana");
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(usuarioService.buscarPorId(2L)).thenReturn(ana);
            when(repository.save(any(Equipe.class))).thenAnswer(inv -> {
                Equipe salva = inv.getArgument(0);
                salva.setIdEquipe(4L);
                return salva;
            });

            mvc.perform(comJson(post("/api/equipes"), """
                            {"nome":"Backend","descricao":"Time da API","idProjeto":1,"idUsuario":2}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/equipes/4"))
                    .andExpect(jsonPath("$.idEquipe").value(4))
                    .andExpect(jsonPath("$.projeto.idProjeto").value(1))
                    .andExpect(jsonPath("$.usuario.id").value(2));

            ArgumentCaptor<Equipe> enviada = ArgumentCaptor.forClass(Equipe.class);
            verify(repository).save(enviada.capture());
            assertSame(projeto, enviada.getValue().getProjeto());
            assertSame(ana, enviada.getValue().getUsuario());
        }

        @Test
        @DisplayName("POST /api/equipes sem projeto e sem usuário salva sem consultar os services")
        void criarSemVinculos() throws Exception {
            when(repository.save(any(Equipe.class))).thenAnswer(inv -> {
                Equipe salva = inv.getArgument(0);
                salva.setIdEquipe(5L);
                return salva;
            });

            mvc.perform(comJson(post("/api/equipes"), """
                            {"nome":"Solta"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.projeto").doesNotExist())
                    .andExpect(jsonPath("$.usuario").doesNotExist());

            verifyNoInteractions(projetoService, usuarioService);
        }

        @Test
        @DisplayName("POST /api/equipes sem nome devolve 400 e não salva")
        void criarSemNome() throws Exception {
            mvc.perform(comJson(post("/api/equipes"), """
                            {"nome":"  ","descricao":"sem nome"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome da equipe é obrigatório"));

            verifyNoInteractions(repository, projetoService, usuarioService);
        }

        @Test
        @DisplayName("PUT /api/equipes/{id} altera os dados e troca os vínculos")
        void atualizar() throws Exception {
            Usuario lider = usuario(2L, "Ana", "ana");
            Equipe existente = equipe(4L, "Antiga", null, lider);
            Projeto projeto = projeto(1L, "Site novo", "EM_ANDAMENTO");
            when(repository.findById(4L)).thenReturn(Optional.of(existente));
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(repository.save(any(Equipe.class))).thenAnswer(inv -> inv.getArgument(0));

            mvc.perform(comJson(put("/api/equipes/4"), """
                            {"nome":"Nova","descricao":"Time de backend","idProjeto":1}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEquipe").value(4))
                    .andExpect(jsonPath("$.nome").value("Nova"))
                    .andExpect(jsonPath("$.projeto.nome").value("Site novo"));

            assertEquals("Nova", existente.getNome());
            assertEquals("Time de backend", existente.getDescricao());
            assertSame(projeto, existente.getProjeto());
            assertNull(existente.getUsuario());
        }

        @Test
        @DisplayName("PUT /api/equipes/{id} inexistente devolve 404 e não salva")
        void atualizarInexistente() throws Exception {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            mvc.perform(comJson(put("/api/equipes/99"), """
                            {"nome":"Nova"}
                            """))
                    .andExpect(status().isNotFound());

            verify(repository, never()).save(any(Equipe.class));
        }

        @Test
        @DisplayName("GET /api/equipes sem registros devolve lista vazia")
        void listarVazio() throws Exception {
            when(repository.findAll()).thenReturn(List.of());

            mvc.perform(get("/api/equipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("POST /api/equipes com projeto inexistente vira 404 e não salva")
        void criarComProjetoInexistente() throws Exception {
            when(projetoService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(comJson(post("/api/equipes"), """
                            {"nome":"Backend","idProjeto":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(repository, never()).save(any(Equipe.class));
        }

        @Test
        @DisplayName("POST /api/equipes com usuário inexistente vira 404 e não salva")
        void criarComUsuarioInexistente() throws Exception {
            when(usuarioService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Usuario 99 nao encontrado"));

            mvc.perform(comJson(post("/api/equipes"), """
                            {"nome":"Backend","idUsuario":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(repository, never()).save(any(Equipe.class));
        }

        @Test
        @DisplayName("PUT /api/equipes/{id} com projeto inexistente vira 404 e não salva")
        void atualizarComProjetoInexistente() throws Exception {
            when(repository.findById(4L)).thenReturn(Optional.of(equipe(4L, "Backend", null, null)));
            when(projetoService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Projeto 99 nao encontrado"));

            mvc.perform(comJson(put("/api/equipes/4"), """
                            {"nome":"Backend","idProjeto":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(repository, never()).save(any(Equipe.class));
        }

        @Test
        @DisplayName("PUT /api/equipes/{id} com corpo inválido devolve 400 e não consulta nada")
        void atualizarInvalido() throws Exception {
            mvc.perform(comJson(put("/api/equipes/4"), "{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome da equipe é obrigatório"));

            verifyNoInteractions(repository, projetoService, usuarioService);
        }

        @Test
        @DisplayName("DELETE /api/equipes/{id} remove e devolve 204")
        void excluir() throws Exception {
            Equipe existente = equipe(4L, "Backend", null, null);
            when(repository.findById(4L)).thenReturn(Optional.of(existente));

            mvc.perform(delete("/api/equipes/4"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(repository).delete(existente);
        }

        @Test
        @DisplayName("DELETE /api/equipes/{id} inexistente devolve 404 e não remove")
        void excluirInexistente() throws Exception {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            mvc.perform(delete("/api/equipes/99"))
                    .andExpect(status().isNotFound());

            verify(repository, never()).delete(any(Equipe.class));
        }
    }

    // Anexos
    @Nested
    @DisplayName("AnexoController")
    class Anexos {

        private AnexoService service;
        private TarefaService tarefaService;
        private UsuarioService usuarioService;
        private MockMvc mvc;

        @BeforeEach
        void preparar() {
            service = mock(AnexoService.class);
            tarefaService = mock(TarefaService.class);
            usuarioService = mock(UsuarioService.class);
            mvc = criarMvc(new AnexoController(service, tarefaService, usuarioService));
        }

        @Test
        @DisplayName("GET /api/anexos sem filtro lista todos")
        void listarTodos() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(service.listarTodos()).thenReturn(List.of(
                    anexo(1L, tarefa, null),
                    anexo(2L, tarefa, null)));

            mvc.perform(get("/api/anexos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].idTarefa").value(10))
                    .andExpect(jsonPath("$[0].nomeArquivo").value("wireframe.pdf"));

            verify(service, never()).listarPorTarefa(anyLong());
        }

        @Test
        @DisplayName("GET /api/anexos?tarefaId= lista só os anexos da tarefa")
        void listarPorTarefa() throws Exception {
            Tarefa tarefa = tarefa(5L, "Criar home", projeto(1L, "Site novo", "ATIVO"), null);
            when(service.listarPorTarefa(5L)).thenReturn(List.of(anexo(1L, tarefa, null)));

            mvc.perform(get("/api/anexos?tarefaId=5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].idTarefa").value(5));

            verify(service, never()).listarTodos();
        }

        @Test
        @DisplayName("GET /api/anexos/{id} devolve o anexo com o usuário resumido")
        void buscarPorId() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(service.buscarPorId(1L)).thenReturn(anexo(1L, tarefa, usuario(3L, "Ana", "ana")));

            mvc.perform(get("/api/anexos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.urlArquivo").value("https://exemplo.com/wireframe.pdf"))
                    .andExpect(jsonPath("$.tipoArquivo").value("application/pdf"))
                    .andExpect(jsonPath("$.usuario.login").value("ana"))
                    .andExpect(jsonPath("$.usuario.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/anexos/{id} inexistente vira 404")
        void buscarInexistente() throws Exception {
            when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Anexo 99 nao encontrado"));

            mvc.perform(get("/api/anexos/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/anexos busca tarefa e usuário, cria e devolve 201 com Location")
        void criar() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            Usuario ana = usuario(3L, "Ana", "ana");
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(usuarioService.buscarPorId(3L)).thenReturn(ana);
            when(service.salvar(any(Anexo.class))).thenAnswer(inv -> {
                Anexo salvo = inv.getArgument(0);
                salvo.setId(8L);
                return salvo;
            });

            mvc.perform(comJson(post("/api/anexos"), """
                            {"idTarefa":10,"nomeArquivo":"wireframe.pdf",
                             "urlArquivo":"https://exemplo.com/wireframe.pdf",
                             "tipoArquivo":"application/pdf","idUsuario":3}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/anexos/8"))
                    .andExpect(jsonPath("$.id").value(8))
                    .andExpect(jsonPath("$.idTarefa").value(10))
                    .andExpect(jsonPath("$.usuario.login").value("ana"));

            ArgumentCaptor<Anexo> enviado = ArgumentCaptor.forClass(Anexo.class);
            verify(service).salvar(enviado.capture());
            assertSame(tarefa, enviado.getValue().getTarefa());
            assertSame(ana, enviado.getValue().getUsuario());
        }

        @Test
        @DisplayName("POST /api/anexos sem idUsuario cria o anexo sem consultar usuários")
        void criarSemUsuario() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(service.salvar(any(Anexo.class))).thenAnswer(inv -> {
                Anexo salvo = inv.getArgument(0);
                salvo.setId(9L);
                return salvo;
            });

            mvc.perform(comJson(post("/api/anexos"), """
                            {"idTarefa":10,"nomeArquivo":"nota.txt","urlArquivo":"https://exemplo.com/nota.txt"}
                            """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.usuario").doesNotExist());

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("POST /api/anexos sem campos obrigatórios devolve 400 por campo")
        void criarSemObrigatorios() throws Exception {
            mvc.perform(comJson(post("/api/anexos"), "{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.idTarefa").value("O anexo precisa estar vinculado a uma tarefa"))
                    .andExpect(jsonPath("$.nomeArquivo").value("O nome do arquivo é obrigatório"))
                    .andExpect(jsonPath("$.urlArquivo").value("A URL do arquivo é obrigatória"));

            verifyNoInteractions(service, tarefaService, usuarioService);
        }

        @Test
        @DisplayName("POST /api/anexos com tarefa inexistente vira 404 e não salva")
        void criarComTarefaInexistente() throws Exception {
            when(tarefaService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Tarefa 99 nao encontrada"));

            mvc.perform(comJson(post("/api/anexos"), """
                            {"idTarefa":99,"nomeArquivo":"nota.txt","urlArquivo":"https://exemplo.com/nota.txt"}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).salvar(any(Anexo.class));
        }

        @Test
        @DisplayName("PUT /api/anexos/{id} atualiza e devolve 200")
        void atualizar() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(service.atualizar(eq(8L), any(Anexo.class))).thenAnswer(inv -> {
                Anexo atualizado = inv.getArgument(1);
                atualizado.setId(8L);
                return atualizado;
            });

            mvc.perform(comJson(put("/api/anexos/8"), """
                            {"idTarefa":10,"nomeArquivo":"wireframe-v2.pdf",
                             "urlArquivo":"https://exemplo.com/wireframe-v2.pdf"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(8))
                    .andExpect(jsonPath("$.nomeArquivo").value("wireframe-v2.pdf"));
        }

        @Test
        @DisplayName("PUT /api/anexos/{id} com corpo inválido devolve 400 e não chama o service")
        void atualizarInvalido() throws Exception {
            mvc.perform(comJson(put("/api/anexos/8"), """
                            {"idTarefa":10,"nomeArquivo":"  ","urlArquivo":"https://exemplo.com/a.pdf"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nomeArquivo").value("O nome do arquivo é obrigatório"));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("POST /api/anexos com usuário inexistente vira 404 e não salva")
        void criarComUsuarioInexistente() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(usuarioService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Usuario 99 nao encontrado"));

            mvc.perform(comJson(post("/api/anexos"), """
                            {"idTarefa":10,"nomeArquivo":"nota.txt",
                             "urlArquivo":"https://exemplo.com/nota.txt","idUsuario":99}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).salvar(any(Anexo.class));
        }

        @Test
        @DisplayName("PUT /api/anexos/{id} com tarefa inexistente vira 404 e não atualiza")
        void atualizarComTarefaInexistente() throws Exception {
            when(tarefaService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Tarefa 99 nao encontrada"));

            mvc.perform(comJson(put("/api/anexos/8"), """
                            {"idTarefa":99,"nomeArquivo":"nota.txt","urlArquivo":"https://exemplo.com/nota.txt"}
                            """))
                    .andExpect(status().isNotFound());

            verify(service, never()).atualizar(anyLong(), any(Anexo.class));
        }

        @Test
        @DisplayName("PUT /api/anexos/{id} inexistente vira 404")
        void atualizarInexistente() throws Exception {
            Tarefa tarefa = tarefa(10L, "Criar login", projeto(1L, "Site novo", "ATIVO"), null);
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(service.atualizar(eq(99L), any(Anexo.class)))
                    .thenThrow(new ResourceNotFoundException("Anexo 99 nao encontrado"));

            mvc.perform(comJson(put("/api/anexos/99"), """
                            {"idTarefa":10,"nomeArquivo":"nota.txt","urlArquivo":"https://exemplo.com/nota.txt"}
                            """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/anexos/{id} inexistente vira 404")
        void excluirInexistente() throws Exception {
            doThrow(new ResourceNotFoundException("Anexo 99 nao encontrado")).when(service).excluir(99L);

            mvc.perform(delete("/api/anexos/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/anexos/{id} devolve 204")
        void excluir() throws Exception {
            mvc.perform(delete("/api/anexos/8"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).excluir(8L);
        }
    }
}
