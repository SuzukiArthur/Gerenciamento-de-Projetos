# Gerenciamento de Projetos

**Integrantes:** Alice Martins Miranda, Arthur Souza Ribeiro, Arthur Yuji Mendes Suzuki, Felipe Souza de Jesus e Luiz Miguel Mazega Lamas

API REST para acompanhar projetos, tarefas, equipes, usuários e anexos.]

## O que tem aqui

- CRUD de usuários, projetos, tarefas, equipes e anexos
- Tarefas filtradas por projeto ou por responsável, e anexos filtrados por tarefa
- Listagem de projetos com paginação e ordenação
- Validação dos dados de entrada, com o erro apontando o campo
- Login com HTTP Basic usando os usuários do próprio banco (senha em BCrypt)
- Documentação interativa com Swagger

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

## Como rodar

Você vai precisar de JDK 21, Maven e MySQL 8+.

**1. Banco.** O script cria o `gestao_projetos` com todas as tabelas:

```bash
mysql -u root -p < Gerenciamentoprojeto.sql
```

Duas coisas do script ainda não batem com as entidades. Rode isto logo depois (ou corrija direto no `.sql`):

```sql
USE gestao_projetos;

-- a entidade Projeto espera "descricao", sem acento
ALTER TABLE PROJETOS CHANGE COLUMN `DESCRIÇÃO` descricao TEXT;

-- a entidade Anexo guarda quem enviou o arquivo, mas a coluna não existe
ALTER TABLE ANEXO
  ADD COLUMN id_usuario INT NULL,
  ADD CONSTRAINT fk_anexo_usuario FOREIGN KEY (id_usuario)
      REFERENCES USUARIO(Id) ON DELETE SET NULL ON UPDATE CASCADE;
```

**2. Conexão.** O `application.properties` já aponta para `localhost:3306/gestao_projetos` com o usuário `root`. Se a sua senha for outra, defina as variáveis de ambiente `DB_USER` e `DB_PASSWORD` antes de subir. O Hibernate não mexe no schema (`ddl-auto=none`), quem manda é o script.

**3. Subir a aplicação.**

```bash
cd gerenciador-projetos
mvn spring-boot:run
```

A API fica em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui/index.html`.

## Autenticação

Tudo pede login (HTTP Basic), menos o Swagger e o cadastro de usuário (`POST /api/usuarios`). As credenciais são as da tabela `USUARIO`, e a `funcao` vira o perfil: `ADMIN` vira `ROLE_ADMIN`, e quem não tem função fica como `USER`. As rotas de `/api/usuarios` (fora o cadastro) só aceitam ADMIN; as demais aceitam qualquer usuário logado.

Como o cadastro é aberto, para o primeiro admin funciona assim:

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Admin","login":"admin","senha":"admin123","funcao":"ADMIN"}'
```

Depois é só mandar as credenciais nas outras chamadas:

```bash
curl -u admin:admin123 http://localhost:8080/api/projetos
```

*Quem se cadastra escolhe a própria função. Na collection do Postman as variáveis `username` e `password` vêm como `admin`/`admin`; troque pelo login e pela senha que você criou.

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

## Respostas e erros

- `200` nas consultas e atualizações, `201` no POST (com o `Location` do novo recurso) e `204` no DELETE.
- `400` quando a validação falha. O corpo vem no formato `campo: mensagem`:

  ```json
  {
    "login": "O login deve ter entre 3 e 50 caracteres",
    "senha": "A senha deve ter no mínimo 6 caracteres"
  }
  ```

- `401` sem login ou com credenciais erradas, e `403` quando o usuário logado não é ADMIN numa rota de usuários.
- `404` com a mensagem em texto quando o registro não existe, por exemplo `Equipe 99 não encontrada`.

> **Atenção:** hoje só a busca de equipe por id devolve esse 404 de verdade. Nos services de usuário, projeto, tarefa e anexo, o "não encontrado" é um `RuntimeException` comum que o handler global não trata, então a API responde 500 (inclusive quando um `idProjeto` ou `idUsuario` enviado no corpo não existe).

## Modelo de dados

Um projeto tem várias tarefas e várias equipes, e uma tarefa tem vários anexos. Um usuário pode ser responsável por tarefas, líder de equipes e autor de anexos.

Sobre as exclusões: ao apagar um projeto, as tarefas dele vão junto (as equipes só perdem o vínculo); ao apagar uma tarefa, os anexos também; ao apagar um usuário, os vínculos dele ficam vazios.

## Testes

```bash
cd gerenciador-projetos
mvn test
```

- `ControllersTest`: os cinco controllers num arquivo só, com MockMvc e os services mockados, sem banco e sem subir o Spring. Confere status HTTP, validação (400), 404, o `Location` do 201, filtros, paginação e se a senha some das respostas. O login e os perfis (Spring Security) ficam de fora.
- `DtoTest`: conversão entre entidade e DTO, e as regras de validação.
- `GerenciadorProjetosApplicationTests`: só confere se o contexto sobe, então precisa do MySQL rodando com o banco criado.
