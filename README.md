# Saúde Cardíaca API

API REST para monitoramento de saúde cardíaca, desenvolvida como projeto prático da disciplina de **Engenharia de Software 2**.

---

## Sobre o Projeto

O sistema permite que usuários registrem e acompanhem indicadores de saúde cardiovascular ao longo do tempo. Cada usuário autenticado pode registrar medições como pressão arterial, frequência cardíaca, nível de oxigenação e peso corporal, além de relatar sintomas. O histórico fica vinculado exclusivamente ao perfil de cada paciente.

---

## Arquitetura

A aplicação segue a **arquitetura em camadas** (Layered Architecture), padrão amplamente adotado em sistemas Spring Boot. As responsabilidades são divididas em camadas bem definidas que se comunicam em sentido único: da camada mais externa (HTTP) até a mais interna (banco de dados).

```
Cliente (HTTP)
     │
     ▼
┌─────────────┐
│  Controller │  ← Recebe requisições, delega ao Service
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  ← Contém a lógica de negócio e validações
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  ← Abstração de acesso ao banco de dados
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   MySQL DB  │  ← Persistência dos dados
└─────────────┘
```

### Segurança (transversal a todas as camadas)

A segurança é tratada de forma transversal por meio de um filtro HTTP que intercepta todas as requisições antes de chegarem aos controllers. O fluxo de autenticação funciona da seguinte forma:

```
Requisição HTTP
      │
      ▼
┌─────────────────┐
│  SecurityFilter │  ← Lê o header "Authorization: Bearer <token>"
└────────┬────────┘
         │ Token válido?
    ┌────┴────┐
   Sim       Não
    │         │
    ▼         ▼
Autentica  HTTP 403
no contexto
```

- Endpoints públicos: `POST /auth/registro` e `POST /auth/login`
- Todos os demais endpoints exigem token JWT válido
- Tokens expiram em 2 horas e são assinados com HMAC256

### Decisões Arquiteturais

| Decisão | Escolha | Motivação |
|---|---|---|
| Autenticação | JWT stateless | Não requer sessão no servidor; escalável |
| Banco de dados | MySQL | Relacional, adequado para dados estruturados de saúde |
| ORM | Hibernate/JPA | Abstração do SQL, mapeamento objeto-relacional |
| Exceções | Handler centralizado | Respostas de erro consistentes em toda a API |
| Senhas | BCrypt | Hash seguro com salt automático |

---

## Modularização

O código está organizado em pacotes com responsabilidades bem definidas dentro do pacote base `com.example.saudecardiaca`:

```
com.example.saudecardiaca/
├── config/          # Configuração de segurança e filtros
├── controller/      # Endpoints REST
├── dto/             # Objetos de transferência de dados
├── exception/       # Tratamento centralizado de erros
├── model/           # Entidades JPA (mapeamento do banco)
├── repository/      # Acesso a dados (Spring Data JPA)
└── service/         # Regras de negócio
```

### `model/` — Entidades

Representam as tabelas do banco de dados.

| Classe | Tabela | Descrição |
|---|---|---|
| `Usuario` | `usuarios` | Dados do paciente; implementa `UserDetails` para integração com Spring Security |
| `Acompanhamento` | `acompanhamentos` | Registro de uma medição cardíaca; vinculado ao `Usuario` por chave estrangeira |

Campos de `Acompanhamento`: `pressaoArterial`, `frequenciaCardiaca`, `nivelOxigenacao`, `pesoCorporal`, `sintomas`, `dataRegistro`.

### `controller/` — Endpoints REST

Recebem requisições HTTP, delegam o processamento ao Service e retornam a resposta.

| Classe | Rota base | Operações |
|---|---|---|
| `AuthController` | `/auth` | `POST /registro` — cadastro de usuário; `POST /login` — autenticação e emissão de JWT |
| `AcompanhamentoController` | `/acompanhamentos` | `POST /` — criar registro; `GET /` — listar histórico do usuário autenticado |

### `service/` — Lógica de Negócio

Contém as regras de negócio e orquestra as operações entre controller e repository.

| Classe | Responsabilidade |
|---|---|
| `UsuarioService` | Cadastro de usuário: valida unicidade de e-mail, confere confirmação de senha, codifica senha com BCrypt |
| `AutenticacaoService` | Implementa `UserDetailsService`; carrega usuário por e-mail para o Spring Security |
| `TokenService` | Gera e valida tokens JWT usando a biblioteca Auth0 (algoritmo HMAC256, expiração de 2h) |
| `AcompanhamentoService` | Cria e busca registros; aplica validações: frequência cardíaca ≥ 0, oxigenação entre 95–100%, pressão no formato `xxx/xx` |

### `repository/` — Acesso a Dados

Interfaces que estendem `JpaRepository`, fornecendo operações CRUD sem necessidade de SQL manual.

| Interface | Queries personalizadas |
|---|---|
| `UsuarioRepository` | `findByEmail(String)`, `existsByEmail(String)` |
| `AcompanhamentoRepository` | `findByUsuarioId(Long)` |

### `dto/` — Objetos de Transferência

Isolam o contrato da API dos modelos internos, evitando exposição desnecessária de campos.

| Classe | Uso |
|---|---|
| `LoginRequestDTO` | Corpo da requisição de login (`email`, `senha`) |
| `RegistroRequestDTO` | Corpo do cadastro (dados pessoais + `confirmarSenha`) |

### `exception/` — Tratamento de Erros

| Classe | Descrição |
|---|---|
| `RegraNegocioException` | Exceção customizada lançada pelas regras de negócio na camada Service |
| `GlobalExceptionHandler` | `@RestControllerAdvice` que captura `RegraNegocioException` e retorna HTTP 400 com corpo padronizado (`timestamp`, `status`, `message`) |

### `config/` — Configuração de Segurança

| Classe | Descrição |
|---|---|
| `SecurityConfig` | Define regras de autorização: rotas públicas vs. protegidas; configura `BCryptPasswordEncoder` e `AuthenticationManager`; desabilita CSRF (API stateless) |
| `SecurityFilter` | Filtro `OncePerRequestFilter` que extrai o JWT do header, valida via `TokenService`, carrega o usuário e popula o `SecurityContextHolder` |

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | — | Autenticação e autorização |
| Spring Data JPA | — | Acesso a dados com Hibernate |
| MySQL | — | Banco de dados relacional |
| Auth0 Java JWT | 4.4.0 | Geração e validação de tokens JWT |
| Lombok | — | Redução de boilerplate (getters, setters, construtores) |
| Maven | — | Gerenciamento de dependências e build |

---

## Configuração e Execução

### Pré-requisitos

- Java 17+
- MySQL rodando localmente na porta `3306`
- Banco de dados `saude_cardiaca` criado

### Configuração

Edite `src/main/resources/application.properties` conforme seu ambiente:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/saude_cardiaca
spring.datasource.username=root
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
server.port=8081
```

### Executando

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8081`.

---

## Testes Automatizados

O projeto possui um conjunto de testes unitários que cobre **100% dos controllers e das regras de negócio (services)**. A configuração dos testes utiliza um banco de dados em memória (**H2 Database**) para garantir o isolamento sem afetar seu banco MySQL.

Para rodar todos os testes, execute o seguinte comando na raiz do projeto:

```bash
./mvnw clean test
```

---

## Endpoints

### Autenticação

```
POST /auth/registro   — Cadastrar novo usuário
POST /auth/login      — Autenticar e receber token JWT
```

### Acompanhamentos (requer token JWT no header)

```
POST /acompanhamentos   — Registrar nova medição
GET  /acompanhamentos   — Listar histórico do usuário autenticado
```

**Header obrigatório para rotas protegidas:**
```
Authorization: Bearer <token>
```
