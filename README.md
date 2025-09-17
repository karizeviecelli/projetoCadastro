# projetoCadastro# Cadastro de Produtos — Spring Boot + Thymeleaf + Bootstrap + SQL

> CRUD enxuto, bonito (com Bootstrap) e pronto para produção didática. Bora colocar esse app pra rodar. 🚀

---

## 1) O que vamos construir

Um CRUD de **Produtos** com:

* Listagem com **busca**, **paginação** e **ordenação**
* **Formulário** de criação/edição com validação (Bean Validation)
* **Remoção** com confirmação
* Layout com **Bootstrap 5** + fragmentos Thymeleaf
* Persistência via **Spring Data JPA**
* Banco **H2** (dev) e **MySQL** (prod) — alternáveis por perfil

**Entidade Produto** (campos sugeridos):

* `id` (Long, auto)
* `nome` (String, obrigatório)
* `descricao` (String, até 500)
* `preco` (BigDecimal, >=0)
* `estoque` (Integer, >=0)
* `ativo` (Boolean)
* `createdAt`/`updatedAt` (LocalDateTime)

---

## 2) Criação do projeto (Spring Initializr)

* **Project:** Maven
* **Language:** Java 17+
* **Dependencies:** Spring Web, Thymeleaf, Spring Data JPA, Validation, Lombok, H2 Database, MySQL Driver (opcional), Spring Boot DevTools (opcional)

Estrutura esperada:

```
src
 └─ main
    ├─ java/com/seuprojeto/produtos
    │   ├─ ProdutosApplication.java
    │   ├─ domain/Produto.java
    │   ├─ repository/ProdutoRepository.java
    │   ├─ service/ProdutoService.java
    │   ├─ controller/ProdutoController.java
    │   └─ config/WebConfig.java (opcional)
    └─ resources
        ├─ application.properties (ou application.yml)
        ├─ templates/
        │   ├─ fragments/_base.html
        │   └─ produtos/
        │       ├─ lista.html
        │       └─ form.html
        └─ static/
            ├─ css/app.css
            └─ js/app.js
```

---

## 3) `pom.xml`

Inclua (ou confira) as dependências:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
  </dependency>
  <dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-core</artifactId>
  </dependency>
</dependencies>
```

---

## 4) `application.yml`

Use **perfis** para alternar H2 (dev) e MySQL (prod):

```yaml
spring:
  profiles:
    active: dev
---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:produtosdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2
---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://localhost:3306/produtosdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

---

## 5) Entidade `Produto`

```java
// Pacote onde está localizada a classe Produto
package com.cadastro_produtos.cadastro.domain;

// Importa classes para manipulação de valores monetários e datas
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Importa anotações para mapeamento JPA e validação de dados
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
// Importa anotações do Lombok para facilitar a criação de métodos e construtores
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indica que a classe é uma entidade JPA e será mapeada para a tabela "produtos"
@Entity
@Table(name = "produtos")
// Lombok: Gera automaticamente getters, setters, construtores e métodos utilitários
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Produto {
    // Identificador único do produto, gerado automaticamente pelo banco
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do produto, obrigatório e limitado a 100 caracteres
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 100, message = "O nome do produto deve ter no máximo 100 caracteres")
    private String nome;

    // Descrição do produto, opcional e limitada a 500 caracteres
    @Size(max = 500, message = "A descrição do produto deve ter no máximo 500 caracteres")
    private String descricao;

    // Preço do produto, obrigatório, maior que zero e com até 10 dígitos inteiros e 2 decimais
    @NotNull(message = "O preço do produto é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço do produto deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "O preço do produto deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal preco;

    // Quantidade em estoque, obrigatório e não pode ser negativo
    @NotNull(message = "O estoque do produto é obrigatório")
    @Min(value = 0, message = "O estoque do produto não pode ser negativo")
    private Integer estoque;

    // Status do produto (ativo/inativo), obrigatório
    @NotNull(message = "O status do produto é obrigatório")
    private Boolean ativo;

    // Datas de criação e atualização do produto
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Método chamado automaticamente antes de salvar um novo produto no banco
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    // Método chamado automaticamente antes de atualizar um produto existente
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    

}

```

---

## 6) Repositório

```java
package com.cadastro_produtos.cadastro.repository;

// Importações necessárias para trabalhar com paginação e repositórios JPA
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cadastro_produtos.cadastro.domain.Produto;

// Interface que representa o repositório para a entidade Produto
// JpaRepository fornece métodos prontos para operações básicas de CRUD (Create, Read, Update, Delete)
// e suporte a paginação e ordenação.
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método personalizado para buscar produtos pelo nome, ignorando maiúsculas e minúsculas.
    // O parâmetro "q" é a string que será usada como critério de busca.
    // Pageable permite que os resultados sejam retornados de forma paginada.
    Page<Produto> findByNomeContainingIgnoreCase(String q, Pageable pageable);
    // Este método gera automaticamente uma consulta baseada no nome do método.
    // "findByNomeContainingIgnoreCase" significa:
    // - "findBy": indica que é uma busca.
    // - "Nome": refere-se ao atributo "nome" da entidade Produto.
    // - "Containing": busca por valores que contenham a string fornecida.
    // - "IgnoreCase": ignora diferenças entre maiúsculas e minúsculas.
}

```

---

## 7) Serviço (opcional, mas saudável)

```java
package com.cadastro_produtos.cadastro.service;

// Importação da classe Produto, que representa a entidade do domínio
import com.cadastro_produtos.cadastro.domain.Produto;
// Importação do repositório ProdutoRepository, que fornece acesso ao banco de dados
import com.cadastro_produtos.cadastro.repository.ProdutoRepository;
// Importação da anotação @RequiredArgsConstructor, que gera um construtor com os atributos finais
import lombok.RequiredArgsConstructor;
// Importações para paginação e manipulação de dados
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Importação da anotação @Service, que marca esta classe como um componente de serviço do Spring
import org.springframework.stereotype.Service;

import java.util.Optional;

// Anotação @Service indica que esta classe é um serviço gerenciado pelo Spring
@Service
// Anotação @RequiredArgsConstructor gera automaticamente um construtor para os atributos finais
@RequiredArgsConstructor
public class ProdutoService {
    // Dependência do repositório ProdutoRepository, usada para acessar o banco de dados
    private final ProdutoRepository repo;

    // Método para listar produtos com suporte a busca por nome e paginação
    public Page<Produto> listar(String q, Pageable pageable) {
        // Se a string de busca (q) for nula ou vazia, retorna todos os produtos paginados
        // Caso contrário, realiza uma busca por nome contendo a string informada, ignorando maiúsculas e minúsculas
        return (q == null || q.isBlank()) ? repo.findAll(pageable)
                : repo.findByNomeContainingIgnoreCase(q, pageable);
    }

    // Método para salvar um novo produto ou atualizar um existente
    public Produto salvar(Produto p) {
        return repo.save(p); // Chama o método save do repositório para persistir o produto
    }

    // Método para buscar um produto pelo seu ID
    public Optional<Produto> porId(Long id) {
        return repo.findById(id); // Retorna um Optional contendo o produto, se encontrado
    }

    // Método para excluir um produto pelo seu ID
    public void excluir(Long id) {
        repo.deleteById(id); // Chama o método deleteById do repositório para remover o produto
    }

    public Object listarTodos() {
        // Retorna todos os produtos do repositório
        return repo.findAll();
    }
}
```

---

## 8) Controller (Thymeleaf)

```java
package com.cadastro_produtos.cadastro.controller;

// Importações necessárias para o funcionamento do controlador
import com.cadastro_produtos.cadastro.domain.Produto; // Entidade Produto
import com.cadastro_produtos.cadastro.service.ProdutoService; // Serviço para manipulação de produtos
import jakarta.validation.Valid; // Validação de dados
import lombok.RequiredArgsConstructor; // Geração automática de construtor para atributos finais
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Logger para registrar informações no console
import org.springframework.data.domain.Page; // Classe para paginação
import org.springframework.data.domain.PageRequest; // Criação de objetos Pageable
import org.springframework.data.domain.Pageable; // Interface para paginação
import org.springframework.data.domain.Sort; // Classe para ordenação
import org.springframework.stereotype.Controller; // Indica que esta classe é um controlador Spring
import org.springframework.ui.Model; // Interface para passar dados para a view
import org.springframework.validation.BindingResult; // Resultado da validação de dados
import org.springframework.web.bind.annotation.*; // Anotações para mapeamento de requisições
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Atributos para redirecionamento

@Controller
@RequestMapping("/produtos") // Define o mapeamento base para todas as rotas deste controlador
@RequiredArgsConstructor // Gera automaticamente um construtor para os atributos finais
public class ProdutoController {

    private static final String PRODUTOS_FORM = "produtos/form"; // Caminho da view do formulário de produtos
    private static final String REDIRECT_PRODUTOS = "redirect:/produtos"; // Redirecionamento para a lista de produtos

    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class); // Logger para registrar informações no console

    private final ProdutoService service; // Dependência do serviço de produtos, usada para acessar as regras de negócio

    // Método para listar produtos com suporte a busca, paginação e ordenação
    @GetMapping
    public String listar(@RequestParam(value = "q", required = false) String q, // Parâmetro de busca
                         @RequestParam(value = "page", defaultValue = "0") int page, // Página atual
                         @RequestParam(value = "size", defaultValue = "10") int size, // Tamanho da página
                         @RequestParam(value = "sort", defaultValue = "nome") String sort, // Campo de ordenação
                         @RequestParam(value = "dir", defaultValue = "asc") String dir, // Direção da ordenação
                         Model model) { // Objeto para passar dados para a view
        logger.info("Listando produtos com os parâmetros: q={}, page={}, size={}, sort={}, dir={}", q, page, size, sort, dir);
        Sort s = Sort.by(dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sort); // Configura a ordenação
        Pageable pageable = PageRequest.of(page, size, s); // Cria um objeto Pageable com as configurações de paginação e ordenação
        Page<Produto> pagina = service.listar(q, pageable); // Busca os produtos com base nos parâmetros fornecidos
        model.addAttribute("pagina", pagina); // Adiciona os dados ao modelo para serem exibidos na view
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        logger.info("Produtos listados com sucesso. Total de itens: {}", pagina.getTotalElements());
        return "produtos/lista"; // Retorna o nome da view para exibir a lista de produtos
    }

    // Método para exibir o formulário de criação de um novo produto
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto()); // Adiciona um novo objeto Produto ao modelo
        return PRODUTOS_FORM; // Retorna o nome da view do formulário
    }

    // Método para salvar um novo produto ou atualizar um existente
    @PostMapping
    public String salvar(@Valid @ModelAttribute("produto") Produto produto, // Produto validado
                         BindingResult br, // Resultado da validação
                         RedirectAttributes ra) { // Atributos para redirecionamento
        if (br.hasErrors()) { // Verifica se há erros de validação
            logger.warn("Erro de validação ao salvar o produto: {}", br.getAllErrors());
            return PRODUTOS_FORM; // Retorna ao formulário em caso de erro
        }
        service.salvar(produto); // Salva o produto usando o serviço
        logger.info("Produto salvo com sucesso: {}", produto);
        return REDIRECT_PRODUTOS; // Redireciona para a lista de produtos
    }

    // Método para exibir o formulário de edição de um produto existente
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        logger.info("Acessando o formulário para editar o produto com ID: {}", id);
        return service.porId(id).map(p -> { // Busca o produto pelo ID e, se encontrado, adiciona ao modelo
            model.addAttribute("produto", p);
            return PRODUTOS_FORM; // Retorna o formulário preenchido
        }).orElseGet(() -> { // Caso o produto não seja encontrado
            logger.warn("Produto com ID {} não encontrado.", id);
            ra.addFlashAttribute("erro", "Produto não encontrado"); // Mensagem de erro
            return REDIRECT_PRODUTOS; // Redireciona para a lista de produtos
        });
    }

    // Método para excluir um produto pelo seu ID
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        logger.info("Excluindo o produto com ID: {}", id);
        service.excluir(id); // Exclui o produto usando o serviço
        ra.addFlashAttribute("sucesso", "Produto excluído!"); // Mensagem de sucesso
        return REDIRECT_PRODUTOS; // Redireciona para a lista de produtos
    }
}

```

---

## 9) Fragmento base com Bootstrap (`templates/fragments/_base.html`)

```html
<!DOCTYPE html>
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org"
      th:fragment="html(titulo, conteudo)">
<!-- Declaração do tipo de documento HTML e definição do idioma como português do Brasil -->
<!-- O atributo "th:fragment" define este arquivo como um fragmento reutilizável com dois parâmetros:
     - "titulo": para o título da página.
     - "conteudo": para o conteúdo principal da página. -->

<head>
  <meta charset="utf-8">
  <!-- Define o conjunto de caracteres como UTF-8 para suportar caracteres especiais -->
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Configura o layout responsivo para dispositivos móveis -->

  <!-- Define o título da página. O valor é recebido dinamicamente da página filha -->
  <title th:replace="${titulo}">Produtos</title>

  <!-- Importa o CSS do Bootstrap para estilização -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <!-- Importa um arquivo CSS personalizado localizado na pasta "css" do projeto -->
  <link th:href="@{/css/app.css}" rel="stylesheet">
</head>

<body>
<nav class="navbar navbar-dark bg-dark mb-4">
  <!-- Cria uma barra de navegação com fundo escuro e margem inferior -->
  <div class="container">
    <!-- Define um contêiner para centralizar o conteúdo -->
    <a class="navbar-brand" th:href="@{/produtos}">Produtos</a>
    <!-- Link para a página principal dos produtos -->
  </div>
</nav>

<main class="container">
  <!-- Define o conteúdo principal da página -->
  <!-- O atributo "th:insert" insere dinamicamente o conteúdo da seção da página filha -->
  <div th:insert="${conteudo}">[conteúdo]</div>
</main>

<!-- Importa o JavaScript do Bootstrap para funcionalidades interativas -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<!-- Importa um arquivo JavaScript personalizado localizado na pasta "js" do projeto -->
<script th:src="@{/js/app.js}"></script>
</body>
</html>


```

---

## 10) Lista (`templates/produtos/lista.html`)

```html
<!doctype html>
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org"
      th:replace="fragments/_base :: html(~{::title}, ~{::section})">
<!-- Declaração do tipo de documento HTML e definição do idioma como português do Brasil -->
<!-- O atributo "th:replace" substitui o conteúdo pelo fragmento "_base.html", reutilizando o layout base -->
<head>
  <title>Produtos</title>
  <!-- Define o título da página -->
</head>
<section>
  <div class="d-flex justify-content-between align-items-center mb-3">
    <!-- Cabeçalho da página com título e botão para adicionar novo produto -->
    <h1 class="h3">Produtos</h1>
    <a class="btn btn-primary" th:href="@{/produtos/novo}">Novo</a>
    <!-- Link para a página de criação de um novo produto -->
  </div>

  <form class="row g-2 mb-3" method="get">
    <!-- Formulário para busca de produtos -->
    <div class="col-auto">
      <input type="text" class="form-control" name="q" th:value="${q}" placeholder="Buscar por nome...">
      <!-- Campo de entrada para busca, preenchido com o valor atual do parâmetro "q" -->
    </div>
    <div class="col-auto">
      <button class="btn btn-outline-secondary" type="submit">Buscar</button>
      <!-- Botão para submeter o formulário de busca -->
    </div>
  </form>

  <div th:if="${sucesso}" class="alert alert-success" th:text="${sucesso}"></div>
  <!-- Exibe uma mensagem de sucesso, se existir -->
  <div th:if="${erro}" class="alert alert-danger" th:text="${erro}"></div>
  <!-- Exibe uma mensagem de erro, se existir -->

  <table class="table table-striped align-middle">
    <!-- Tabela para exibição dos produtos -->
    <thead>
      <tr>
        <th>
          <a th:href="@{|/produtos?q=${q}&sort=nome&dir=${dir=='asc'?'desc':'asc'}|}">Nome</a>
          <!-- Link para ordenar os produtos pelo nome -->
        </th>
        <th>Preço</th>
        <th>Estoque</th>
        <th>Ativo</th>
        <th style="width: 160px">Ações</th>
        <!-- Coluna para ações (editar e excluir) -->
      </tr>
    </thead>
    <tbody>
      <tr th:each="p : ${pagina.content}">
        <!-- Itera sobre os produtos na página atual -->
        <td th:text="${p.nome}"></td>
        <!-- Exibe o nome do produto -->
        <td th:text="${#numbers.formatDecimal(p.preco, 1, 'POINT', 2, 'COMMA')}"></td>
        <!-- Exibe o preço do produto formatado -->
        <td th:text="${p.estoque}"></td>
        <!-- Exibe a quantidade em estoque -->
        <td>
          <span th:classappend="${p.ativo} ? 'badge bg-success' : 'badge bg-secondary'" th:text="${p.ativo} ? 'Sim' : 'Não'"></span>
          <!-- Exibe se o produto está ativo ou não, com cores diferentes -->
        </td>
        <td>
          <a class="btn btn-sm btn-outline-primary" th:href="@{|/produtos/editar/${p.id}|}">Editar</a>
          <!-- Link para editar o produto -->
          <form th:action="@{|/produtos/excluir/${p.id}|}" method="post" class="d-inline" onsubmit="return confirm('Confirma a exclusão?');">
            <!-- Formulário para excluir o produto -->
            <button class="btn btn-sm btn-outline-danger" type="submit">Excluir</button>
            <!-- Botão para excluir o produto -->
          </form>
        </td>
      </tr>
    </tbody>
  </table>

  <nav th:if="${pagina.totalPages > 1}">
    <!-- Navegação entre páginas, exibida apenas se houver mais de uma página -->
    <ul class="pagination">
      <li class="page-item" th:classappend="${pagina.first} ? 'disabled'">
        <!-- Botão "Anterior", desativado se estiver na primeira página -->
        <a class="page-link" th:href="@{|/produtos?page=${pagina.number-1}&q=${q}|}">Anterior</a>
      </li>
      <li class="page-item" th:each="i : ${#numbers.sequence(0, pagina.totalPages-1)}" th:classappend="${i==pagina.number}? 'active'">
        <!-- Botões para cada página, com o botão da página atual destacado -->
        <a class="page-link" th:text="${i+1}" th:href="@{|/produtos?page=${i}&q=${q}|}"></a>
      </li>
      <li class="page-item" th:classappend="${pagina.last} ? 'disabled'">
        <!-- Botão "Próxima", desativado se estiver na última página -->
        <a class="page-link" th:href="@{|/produtos?page=${pagina.number+1}&q=${q}|}">Próxima</a>
      </li>
    </ul>
  </nav>
</section>
</html>
```

---

## 11) Formulário (`templates/produtos/form.html`)

```html
<!doctype html>
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org"
      th:replace="fragments/_base :: html(~{::title}, ~{::section})">
<!-- Declaração do tipo de documento HTML e definição do idioma como português do Brasil -->
<!-- O atributo "th:replace" substitui o conteúdo pelo fragmento "_base.html" -->
<head>
  <title th:text="${produto.id} != null ? 'Editar Produto' : 'Novo Produto'"></title>
  <!-- Define o título da página dinamicamente:
       - "Editar Produto" se o produto já existir (tem ID).
       - "Novo Produto" se for um novo produto (sem ID). -->
</head>
<section>
  <h1 class="h3 mb-3" th:text="${produto.id} != null ? 'Editar Produto' : 'Novo Produto'">Novo Produto</h1>
  <!-- Define o cabeçalho da página dinamicamente:
       - "Editar Produto" se o produto já existir (tem ID).
       - "Novo Produto" se for um novo produto (sem ID). -->

  <form th:action="@{/produtos}" th:object="${produto}" method="post" class="row g-3">
    <!-- Formulário para criar ou editar um produto:
         - "th:action" define a URL para onde o formulário será enviado.
         - "th:object" associa o formulário ao objeto "produto".
         - "method=post" define o método HTTP usado para enviar os dados. -->

    <input type="hidden" th:if="${produto.id}" th:field="*{id}">
    <!-- Campo oculto para o ID do produto:
         - Só é exibido se o produto já existir (tem ID). -->

    <div class="col-md-6">
      <label class="form-label" for="nome">Nome</label>
      <input class="form-control" id="nome" th:field="*{nome}">
      <!-- Campo de entrada para o nome do produto:
           - "th:field" associa o campo ao atributo "nome" do objeto "produto". -->
      <div class="text-danger" th:if="${#fields.hasErrors('nome')}" th:errors="*{nome}"></div>
      <!-- Exibe mensagens de erro de validação para o campo "nome", se existirem. -->
    </div>

    <div class="col-md-6">
      <label class="form-label" for="preco">Preço</label>
      <input type="number" step="0.01" class="form-control" th:field="*{preco}">
      <!-- Campo de entrada para o preço do produto:
           - "type=number" define que o campo aceita apenas números.
           - "step=0.01" permite valores decimais com duas casas. -->
      <div class="text-danger" th:if="${#fields.hasErrors('preco')}" th:errors="*{preco}"></div>
      <!-- Exibe mensagens de erro de validação para o campo "preco", se existirem. -->
    </div>

    <div class="col-md-6">
      <label class="form-label" for="estoque">Estoque</label>
      <input type="number" class="form-control" th:field="*{estoque}">
      <!-- Campo de entrada para a quantidade em estoque:
           - "type=number" define que o campo aceita apenas números inteiros. -->
      <div class="text-danger" th:if="${#fields.hasErrors('estoque')}" th:errors="*{estoque}"></div>
      <!-- Exibe mensagens de erro de validação para o campo "estoque", se existirem. -->
    </div>

    <div class="col-md-6">
      <label class="form-label" for="ativo">Ativo</label>
      <select class="form-select" th:field="*{ativo}">
        <!-- Campo de seleção para o status "ativo" do produto:
             - "th:field" associa o campo ao atributo "ativo" do objeto "produto". -->
        <option th:value="true">Sim</option>
        <option th:value="false">Não</option>
        <!-- Opções para definir se o produto está ativo ou não. -->
      </select>
    </div>

    <div class="col-12">
      <label class="form-label" for="descricao">Descrição</label>
      <textarea class="form-control" th:field="*{descricao}" rows="3"></textarea>
      <!-- Campo de texto para a descrição do produto:
           - "rows=3" define a altura inicial do campo. -->
    </div>

    <div class="col-12 d-flex gap-2">
      <button class="btn btn-primary" type="submit">Salvar</button>
      <!-- Botão para enviar o formulário. -->
      <a class="btn btn-outline-secondary" th:href="@{/produtos}">Cancelar</a>
      <!-- Link para cancelar e voltar à lista de produtos. -->
    </div>
  </form>
</section>
</html>
```

---

## 12) CSS e JS (opcionais)

`/static/css/app.css`

```css
body { padding-bottom: 48px; }
```

`/static/js/app.js`

```js
// espaço para pequenos aprimoramentos
```

---

## 13) Dados de exemplo (CommandLineRunner)

```java
package com.cadastro_produtos.cadastro;

// Importação da interface CommandLineRunner, usada para executar código ao iniciar a aplicação
import org.springframework.boot.CommandLineRunner;
// Importação da anotação @Bean, que indica que o método retorna um bean gerenciado pelo Spring
import org.springframework.context.annotation.Bean;
// Importação da anotação @Configuration, que indica que esta classe contém definições de beans
import org.springframework.context.annotation.Configuration;

import com.cadastro_produtos.cadastro.domain.Produto; // Entidade Produto
import com.cadastro_produtos.cadastro.repository.ProdutoRepository; // Repositório para manipulação de produtos

import java.math.BigDecimal; // Classe para manipulação de valores monetários

// Classe de configuração para inicializar dados no banco ao iniciar a aplicação
@Configuration
public class BootstrapData {

    // Define um bean do tipo CommandLineRunner, que será executado ao iniciar a aplicação
    @Bean
    CommandLineRunner seed(ProdutoRepository repo) {
        // O método retorna uma função lambda que será executada ao iniciar a aplicação
        return args -> {
            // Verifica se o repositório está vazio (não há produtos cadastrados)
            if (repo.count() == 0) {
                // Insere produtos no banco de dados usando o repositório
                repo.save(Produto.builder()
                        .nome("Caneta Azul") // Nome do produto
                        .preco(new BigDecimal("3.50")) // Preço do produto
                        .estoque(100) // Quantidade em estoque
                        .ativo(true) // Produto ativo
                        .descricao("Clássica") // Descrição do produto
                        .build()); // Cria e salva o produto

                repo.save(Produto.builder()
                        .nome("Caderno 100fl")
                        .preco(new BigDecimal("15.90"))
                        .estoque(50)
                        .ativo(true)
                        .build());

                repo.save(Produto.builder()
                        .nome("Mochila")
                        .preco(new BigDecimal("120.00"))
                        .estoque(10)
                        .ativo(true)
                        .build());
            }
        };
    }
}
```

---

## 14) Execução

* **Dev (H2):** `mvn spring-boot:run` e acesse `http://localhost:8080/produtos` (console H2 em `/h2`).
* **Prod (MySQL):** inicie o MySQL, ajuste credenciais no `application.yml` e rode com `-Dspring.profiles.active=prod`.

---

## 15) Extras que cabem fácil

* **Validação customizada** (ex.: preço mínimo quando ativo=true)
* **Upload de imagem** por produto (salvar em `/uploads` ou S3)
* **DTO + Mapper** (MapStruct) para separar camada web da entidade
* **Autenticação** (Spring Security) para CRUD protegido
* **Mensageria/Logs**: usar `@ControllerAdvice` + `Slf4j`

---

## 16) Dúvidas comuns (FAQ)

* **“Preciso de Service?”** Tecnicamente não para CRUD simples, mas ajuda a manter regras de negócio fora do controller.
* **“Paginar como API?”** Substitua Thymeleaf por endpoints REST + fetch/axios no front.
* **“E a máscara de moeda?”** Use `Intl.NumberFormat` no browser ou formate no backend.

---

## 17) Teste rápido do fluxo

1. Acesse `/produtos` → veja a lista paginada
2. Clique **Novo** → preencha e salve → Flash de sucesso
3. Edite um item → altere preço/estoque → salve
4. Busque por nome → paginação preserva a query
5. Exclua com confirmação → tchau, item 👋

---

## 18) Próximos passos (quando quiser turbinar)

* **Relatórios** (PDF/Excel)
* **Campos: categoria, código SKU, unidade de medida**
* **Integração com carrinho/ordens**
* **Auditoria (Envers)**
* **Docker Compose** (subir app + MySQL num comando)

---



---

##  **README.md** 👇

````markdown
# Cadastro de Produtos — Spring Boot + Thymeleaf + Bootstrap + SQL

CRUD completo para gestão de produtos com listagem, busca, paginação, ordenação e validação. Front server-side com **Thymeleaf + Bootstrap 5** e persistência via **Spring Data JPA**. Perfis para **H2 (dev)** e **MySQL (prod)**.

> Feito para ensino/aprendizagem: código limpo, camadas claras, validação, seed e instruções passo a passo. ✨

## ✨ Features
- ✅ CRUD de Produtos (nome, preço, estoque, descrição, ativo)
- 🔎 Busca por nome
- 🧭 Paginação e ordenação
- ✅ Validação com Bean Validation
- 🎨 UI com Bootstrap 5 e fragmentos Thymeleaf
- 🗄️ H2 em memória (dev) e MySQL (prod)
- 🌱 Seed inicial (CommandLineRunner)

## 🏗️ Stack
- Java 17+, Spring Boot, Spring Web, Spring Data JPA, Validation, Thymeleaf, Lombok
- H2 (dev) / MySQL (prod)

## 🚀 Comece agora
### 1) Clonar o projeto
```bash
git clone https://github.com/karizeviecelli/projetoCadastro.git
cd cadastro
````

### 2) Rodar em **dev** (H2)

```bash
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

Acesse: `http://localhost:8080/produtos`
Console H2: `http://localhost:8080/h2` (JDBC URL: `jdbc:h2:mem:produtosdb`)

### 3) Rodar em **prod** (MySQL)

Crie um banco `produtosdb` (ou use `createDatabaseIfNotExist=true`). Ajuste `username/password` no `application.yml` se necessário. Depois:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## ⚙️ Configuração (profiles)

Arquivo: `src/main/resources/application.yml`

* `dev` (padrão): H2 em memória, DDL auto `update`, console H2 ligado
* `prod`: MySQL 8+, DDL auto `update`

## 🧩 Endpoints principais

* `GET /produtos` — lista paginada + busca `?q=`
* `GET /produtos/novo` — formulário de criação
* `POST /produtos` — salvar (cria/edita)
* `GET /produtos/editar/{id}` — editar
* `POST /produtos/excluir/{id}` — excluir (com confirmação)

## 🗂️ Estrutura

```
src/main/java/com/seuprojeto/produtos
 ├─ domain/Produto.java
 ├─ repository/ProdutoRepository.java
 ├─ service/ProdutoService.java
 └─ controller/ProdutoController.java
src/main/resources
 ├─ templates/fragments/_base.html
 └─ templates/produtos/{lista.html, form.html}
```

## 🔒 Validação

* `@NotBlank` para nome
* `@DecimalMin("0.0")` + `@Digits` para preço
* `@Min(0)` para estoque

## 🌱 Seed (dados de exemplo)

Em `BootstrapData` é feito o insert de 2–3 produtos caso o repositório esteja vazio.

## 🐳 (Opcional) Docker Compose para MySQL

Crie `docker-compose.yml` na raiz:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8
    container_name: produtos-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: produtosdb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
volumes:
  mysql_data:
```

Suba o banco:

```bash
docker compose up -d
```

Use o profile `prod` para conectar.

## 🧪 Smoke test

1. Acesse `/produtos` e veja a lista com seed
2. Crie um novo produto
3. Edite preço/estoque
4. Busque por nome
5. Exclua e confirme

## 🛠️ Troubleshooting

* **Erro de porta 3306 ocupada**: pare outro MySQL ou mude a porta no compose.
* **`Access denied for user`**: confira usuário/senha e grants.
* **H2 não abre**: verifique o path `/h2` e URL JDBC.
* **Lombok**: habilite annotation processing na IDE.

## 📌 Roadmap (sugestões)

* Upload de imagem por produto
* Autenticação (Spring Security)
* Relatórios (PDF/Excel)
* Auditoria (Envers)
* DTO/Mapper (MapStruct)

## 📄 Licença

Uso educacional. Adapte livremente conforme sua necessidade.

```

