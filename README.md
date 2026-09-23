# Gerenciamento de Projetos

**Integrantes:** Alice Martins Miranda, Arthur Souza Ribeiro, Arthur Yuji Mendes Suzuki, Felipe Souza de Jesus e Luiz Miguel Mazega Lamas

API REST para acompanhar projetos, tarefas, equipes, usuários e anexos.]

## Tecnologias

Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Security), MySQL, Lombok, springdoc-openapi e Maven. Testes com JUnit 5, MockMvc e Mockito.

## Estrutura

```
Gerenciamento-de-Projetos/
|-- Gerenciamentoprojeto.sql                        # script do banco
|-- Gerenciamento-Projetos.postman_collection.json  # requisições prontas
`-- gerenciador-projetos/
    |-- pom.xml
    `-- src/
        |-- main/java/br/unisales/gerenciador_projetos/
        |   |-- controller/    # rotas
        |   |-- service/       # regras de negócio
        |   |-- repository/    # acesso ao banco
        |   |-- entity/        # entidades JPA
        |   |-- dto/           # entrada e saída da API
        |   |-- exception/     # tratamento de erros
        |   |-- security/      # login e permissões
        |   `-- config/        # Swagger
        `-- test/              # testes
```

## Endpoints

Todos ficam sob `/api`:

| Recurso     | Rotas                                                              |
|-------------|--------------------------------------------------------------------|
| `/usuarios` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`            |
| `/projetos` | as mesmas cinco, mais `GET /paginado` e `GET /{id}/tarefas`        |
| `/tarefas`  | as mesmas cinco; o `GET` aceita `?projetoId=` ou `?responsavelId=` |
| `/equipes`  | as mesmas cinco                                                    |
| `/anexos`   | as mesmas cinco; o `GET` aceita `?tarefaId=`                       |

Alguns detalhes:

- A paginação é `/api/projetos/paginado?pagina=0&tamanho=10&ordenarPor=idProjeto`. A resposta traz `conteudo`, `pagina`, `tamanho`, `totalElementos`, `totalPaginas`, `primeira` e `ultima`.
- Se `projetoId` e `responsavelId` vierem juntos em `/tarefas`, só o `projetoId` é considerado.
- As listagens de usuários, projetos e equipes voltam resumidas; a busca por id traz o registro completo.
- O anexo guarda só a URL do arquivo, não existe upload.
- Datas usam o formato `yyyy-MM-dd`.
- Campos obrigatórios: usuário (`nome`, `login`, `senha`), projeto (`nome`), tarefa (`titulo`, `idProjeto`), equipe (`nome`) e anexo (`idTarefa`, `nomeArquivo`, `urlArquivo`). O resto dos campos, e o formato de cada corpo, você vê no Swagger.

Exemplo, criando uma tarefa:

```bash
curl -X POST http://localhost:8080/api/tarefas \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Implementar login",
    "prazo": "2026-03-15",
    "prioridade": "ALTA",
    "status": "PENDENTE",
    "idProjeto": 1,
    "idResponsavel": 1
  }'
```

## Testes

```bash
cd gerenciador-projetos
mvn test
```

- `ControllersTest`: os cinco controllers num arquivo só, com MockMvc e os services mockados, sem banco e sem subir o Spring. Confere status HTTP, validação (400), 404, o `Location` do 201, filtros, paginação e se a senha some das respostas. O login e os perfis (Spring Security) ficam de fora.
- `DtoTest`: conversão entre entidade e DTO, e as regras de validação.
- `GerenciadorProjetosApplicationTests`: só confere se o contexto sobe, então precisa do MySQL rodando com o banco criado.
