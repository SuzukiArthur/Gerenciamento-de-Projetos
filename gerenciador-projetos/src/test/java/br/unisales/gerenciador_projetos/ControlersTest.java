package br.unisales.gerenciador_projetos;

import br.unisales.gerenciador_projetos.controller.AnexoController;
import br.unisales.gerenciador_projetos.controller.EquipeController;
import br.unisales.gerenciador_projetos.controller.ProjetoController;
import br.unisales.gerenciador_projetos.controller.TarefaController;
import br.unisales.gerenciador_projetos.controller.UsuarioController;
import br.unisales.gerenciador_projetos.entity.*;
import br.unisales.gerenciador_projetos.exception.ResourceNotFoundException;
import br.unisales.gerenciador_projetos.repository.EquipeRepository;
import br.unisales.gerenciador_projetos.repository.ProjetoRepository;
import br.unisales.gerenciador_projetos.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        UsuarioController.class,
        ProjetoController.class,
        TarefaController.class,
        EquipeController.class,
        AnexoController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UsuarioService usuarioService;
    @MockBean private ProjetoService projetoService;
    @MockBean private TarefaService tarefaService;
    @MockBean private AnexoService anexoService;
    @MockBean private ProjetoRepository projetoRepository;
    @MockBean private EquipeRepository equipeRepository;

    private Usuario usuario;
    private Projeto projeto;
    private Tarefa tarefa;
    private Equipe equipe;
    private Anexo anexo;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNome("Filipe");
        usuario.setLogin("filipe");
        usuario.setSenha("senhaSuperSecreta");
        usuario.setFuncao("DEV");

        projeto = new Projeto();
        projeto.setIdProjeto(1L);
        projeto.setNome("Sistema Acadêmico");
        projeto.setSupervisor("Maria");
        projeto.setStatus("EM_ANDAMENTO");

        tarefa = new Tarefa();
        tarefa.setIdTarefa(10L);
        tarefa.setTitulo("Implementar login");
        tarefa.setPrazo(LocalDate.of(2026, 3, 15));
        tarefa.setPrioridade("ALTA");
        tarefa.setStatus("PENDENTE");
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(usuario);

        equipe = new Equipe();
        equipe.setIdEquipe(3L);
        equipe.setNome("Squad Backend");
        equipe.setDescricao("Time responsável pela API");
        equipe.setProjeto(projeto);
        equipe.setUsuario(usuario);

        anexo = new Anexo();
        anexo.setId(5L);
        anexo.setNomeArquivo("diagrama.png");
        anexo.setUrlArquivo("https://arquivos.exemplo/diagrama.png");
        anexo.setTipoArquivo("image/png");
        anexo.setTarefa(tarefa);
        anexo.setUsuario(usuario);
    }

    @Nested
    @DisplayName("UsuarioController")
    class Usuarios {

        @Test
        @DisplayName("GET /api/usuarios devolve 200 e a lista resumida")
        void listar() throws Exception {
            when(usuarioService.listarTodos()).thenReturn(List.of(usuario));

            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].login").value("filipe"));
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} não devolve a senha")
        void buscarPorIdNaoVazaSenha() throws Exception {
            when(usuarioService.buscarPorId(7L)).thenReturn(usuario);

            mockMvc.perform(get("/api/usuarios/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Filipe"))
                    .andExpect(jsonPath("$.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} inexistente devolve 404")
        void buscarInexistente() throws Exception {
            when(usuarioService.buscarPorId(999L))
                    .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

            mockMvc.perform(get("/api/usuarios/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Usuário não encontrado"));
        }

        @Test
        @DisplayName("POST /api/usuarios válido devolve 201 e Location")
        void criar() throws Exception {
            when(usuarioService.salvar(any(Usuario.class))).thenReturn(usuario);

            String json = """
                    { "nome": "Filipe", "login": "filipe", "senha": "senha123", "funcao": "DEV" }
                    """;

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/usuarios/7"));
        }

        @Test
        @DisplayName("POST /api/usuarios com senha curta devolve 400")
        void criarComSenhaCurta() throws Exception {
            String json = """
                    { "nome": "Filipe", "login": "filipe", "senha": "123" }
                    """;

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.senha").exists());

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} devolve 200")
        void atualizar() throws Exception {
            when(usuarioService.atualizar(eq(7L), any(Usuario.class))).thenReturn(usuario);

            String json = """
                    { "nome": "Filipe Silva", "login": "filipe", "senha": "senha123", "funcao": "TECH_LEAD" }
                    """;

            mockMvc.perform(put("/api/usuarios/7")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());

            verify(usuarioService).atualizar(eq(7L), any(Usuario.class));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} devolve 204")
        void excluir() throws Exception {
            mockMvc.perform(delete("/api/usuarios/7"))
                    .andExpect(status().isNoContent());

            verify(usuarioService).excluir(7L);
        }
    }

    @Nested
    @DisplayName("ProjetoController")
    class Projetos {

        @Test
        @DisplayName("GET /api/projetos devolve 200")
        void listar() throws Exception {
            when(projetoService.listarTodos()).thenReturn(List.of(projeto));

            mockMvc.perform(get("/api/projetos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Sistema Acadêmico"));
        }

        @Test
        @DisplayName("GET /api/projetos/paginado devolve o envelope de paginação")
        void listarPaginado() throws Exception {
            var page = new PageImpl<>(List.of(projeto), PageRequest.of(0, 10), 1);
            when(projetoRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/projetos/paginado").param("pagina", "0").param("tamanho", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.primeira").value(true))
                    .andExpect(jsonPath("$.conteudo[0].nome").value("Sistema Acadêmico"));
        }

        @Test
        @DisplayName("GET /api/projetos/{id}/tarefas devolve as tarefas do projeto")
        void listarTarefasDoProjeto() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(tarefaService.listarPorProjeto(1L)).thenReturn(List.of(tarefa));

            mockMvc.perform(get("/api/projetos/1/tarefas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].titulo").value("Implementar login"));
        }

        @Test
        @DisplayName("GET /api/projetos/{id} inexistente devolve 404")
        void buscarInexistente() throws Exception {
            when(projetoService.buscarPorId(999L))
                    .thenThrow(new ResourceNotFoundException("Projeto não encontrado"));

            mockMvc.perform(get("/api/projetos/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/projetos sem nome devolve 400")
        void criarSemNome() throws Exception {
            mockMvc.perform(post("/api/projetos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").exists());

            verifyNoInteractions(projetoService);
        }

        @Test
        @DisplayName("POST /api/projetos válido devolve 201")
        void criar() throws Exception {
            when(projetoService.salvar(any(Projeto.class))).thenReturn(projeto);

            String json = """
                    { "nome": "Sistema Acadêmico", "supervisor": "Maria", "status": "PLANEJADO" }
                    """;

            mockMvc.perform(post("/api/projetos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/projetos/1"));
        }

        @Test
        @DisplayName("DELETE /api/projetos/{id} devolve 204")
        void excluir() throws Exception {
            mockMvc.perform(delete("/api/projetos/1"))
                    .andExpect(status().isNoContent());

            verify(projetoService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("TarefaController")
    class Tarefas {

        @Test
        @DisplayName("GET /api/tarefas devolve 200 com projeto e responsável resumidos")
        void listar() throws Exception {
            when(tarefaService.listarTodos()).thenReturn(List.of(tarefa));

            mockMvc.perform(get("/api/tarefas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].projeto.idProjeto").value(1))
                    .andExpect(jsonPath("$[0].responsavel.login").value("filipe"))
                    .andExpect(jsonPath("$[0].responsavel.senha").doesNotExist());
        }

        @Test
        @DisplayName("GET /api/tarefas?projetoId= usa o filtro por projeto")
        void listarPorProjeto() throws Exception {
            when(tarefaService.listarPorProjeto(1L)).thenReturn(List.of(tarefa));

            mockMvc.perform(get("/api/tarefas").param("projetoId", "1"))
                    .andExpect(status().isOk());

            verify(tarefaService).listarPorProjeto(1L);
            verify(tarefaService, never()).listarTodos();
        }

        @Test
        @DisplayName("GET /api/tarefas?responsavelId= usa o filtro por responsável")
        void listarPorResponsavel() throws Exception {
            when(tarefaService.listarPorResponsavel(7L)).thenReturn(List.of(tarefa));

            mockMvc.perform(get("/api/tarefas").param("responsavelId", "7"))
                    .andExpect(status().isOk());

            verify(tarefaService).listarPorResponsavel(7L);
        }

        @Test
        @DisplayName("POST /api/tarefas resolve projeto e responsável pelos ids")
        void criar() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(usuarioService.buscarPorId(7L)).thenReturn(usuario);
            when(tarefaService.salvar(any(Tarefa.class))).thenReturn(tarefa);

            String json = """
                    { "titulo": "Implementar login", "idProjeto": 1, "idResponsavel": 7 }
                    """;

            mockMvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/tarefas/10"));

            verify(projetoService).buscarPorId(1L);
            verify(usuarioService).buscarPorId(7L);
        }

        @Test
        @DisplayName("POST /api/tarefas com título em branco devolve 400")
        void criarComTituloEmBranco() throws Exception {
            String json = """
                    { "titulo": "   ", "idProjeto": 1 }
                    """;

            mockMvc.perform(post("/api/tarefas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.titulo").exists());

            verifyNoInteractions(tarefaService);
        }

        @Test
        @DisplayName("PUT /api/tarefas/{id} devolve 200")
        void atualizar() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(tarefaService.atualizar(eq(10L), any(Tarefa.class))).thenReturn(tarefa);

            String json = """
                    { "titulo": "Implementar login (revisado)", "idProjeto": 1 }
                    """;

            mockMvc.perform(put("/api/tarefas/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/tarefas/{id} devolve 204")
        void excluir() throws Exception {
            mockMvc.perform(delete("/api/tarefas/10"))
                    .andExpect(status().isNoContent());

            verify(tarefaService).excluir(10L);
        }
    }

    @Nested
    @DisplayName("EquipeController")
    class Equipes {

        @Test
        @DisplayName("GET /api/equipes devolve 200")
        void listar() throws Exception {
            when(equipeRepository.findAll()).thenReturn(List.of(equipe));

            mockMvc.perform(get("/api/equipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Squad Backend"));
        }

        @Test
        @DisplayName("GET /api/equipes/{id} inexistente devolve 404")
        void buscarInexistente() throws Exception {
            when(equipeRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/equipes/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/equipes válido devolve 201")
        void criar() throws Exception {
            when(projetoService.buscarPorId(1L)).thenReturn(projeto);
            when(usuarioService.buscarPorId(7L)).thenReturn(usuario);
            when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

            String json = """
                    { "nome": "Squad Backend", "idProjeto": 1, "idUsuario": 7 }
                    """;

            mockMvc.perform(post("/api/equipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/equipes/3"));
        }

        @Test
        @DisplayName("POST /api/equipes sem nome devolve 400")
        void criarSemNome() throws Exception {
            mockMvc.perform(post("/api/equipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(equipeRepository);
        }

        @Test
        @DisplayName("PUT /api/equipes/{id} devolve 200")
        void atualizar() throws Exception {
            when(equipeRepository.findById(3L)).thenReturn(Optional.of(equipe));
            when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

            String json = """
                    { "nome": "Squad Backend v2", "idProjeto": 1, "idUsuario": 7 }
                    """;

            mockMvc.perform(put("/api/equipes/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/equipes/{id} devolve 204")
        void excluir() throws Exception {
            when(equipeRepository.findById(3L)).thenReturn(Optional.of(equipe));

            mockMvc.perform(delete("/api/equipes/3"))
                    .andExpect(status().isNoContent());

            verify(equipeRepository).delete(equipe);
        }
    }

    @Nested
    @DisplayName("AnexoController")
    class Anexos {

        @Test
        @DisplayName("GET /api/anexos devolve 200")
        void listar() throws Exception {
            when(anexoService.listarTodos()).thenReturn(List.of(anexo));

            mockMvc.perform(get("/api/anexos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nomeArquivo").value("diagrama.png"));
        }

        @Test
        @DisplayName("GET /api/anexos?tarefaId= usa o filtro por tarefa")
        void listarPorTarefa() throws Exception {
            when(anexoService.listarPorTarefa(10L)).thenReturn(List.of(anexo));

            mockMvc.perform(get("/api/anexos").param("tarefaId", "10"))
                    .andExpect(status().isOk());

            verify(anexoService).listarPorTarefa(10L);
            verify(anexoService, never()).listarTodos();
        }

        @Test
        @DisplayName("POST /api/anexos resolve a tarefa pelo id")
        void criar() throws Exception {
            when(tarefaService.buscarPorId(10L)).thenReturn(tarefa);
            when(anexoService.salvar(any(Anexo.class))).thenReturn(anexo);

            String json = """
                    { "idTarefa": 10, "nomeArquivo": "diagrama.png", "urlArquivo": "https://arquivos.exemplo/diagrama.png" }
                    """;

            mockMvc.perform(post("/api/anexos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/anexos/5"));
        }

        @Test
        @DisplayName("POST /api/anexos sem idTarefa devolve 400")
        void criarSemTarefa() throws Exception {
            String json = """
                    { "nomeArquivo": "diagrama.png", "urlArquivo": "https://arquivos.exemplo/diagrama.png" }
                    """;

            mockMvc.perform(post("/api/anexos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(anexoService);
        }

        @Test
        @DisplayName("DELETE /api/anexos/{id} devolve 204")
        void excluir() throws Exception {
            mockMvc.perform(delete("/api/anexos/5"))
                    .andExpect(status().isNoContent());

            verify(anexoService).excluir(5L);
        }
    }
}
