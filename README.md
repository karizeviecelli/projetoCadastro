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
package com.seuprojeto.produtos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Produto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120)
    private String nome;

    @Size(max = 500)
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal preco;

    @NotNull(message = "Estoque é obrigatório")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private Integer estoque;

    @NotNull
    private Boolean ativo = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 6) Repositório

```java
package com.seuprojeto.produtos.repository;

import com.seuprojeto.produtos.domain.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Page<Produto> findByNomeContainingIgnoreCase(String q, Pageable pageable);
}
```

---

## 7) Serviço (opcional, mas saudável)

```java
package com.seuprojeto.produtos.service;

import com.seuprojeto.produtos.domain.Produto;
import com.seuprojeto.produtos.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {
    private final ProdutoRepository repo;

    public Page<Produto> listar(String q, Pageable pageable){
        return (q == null || q.isBlank()) ? repo.findAll(pageable)
                : repo.findByNomeContainingIgnoreCase(q, pageable);
    }

    public Produto salvar(Produto p){
        return repo.save(p);
    }

    public Optional<Produto> porId(Long id){
        return repo.findById(id);
    }

    public void excluir(Long id){
        repo.deleteById(id);
    }
}
```

---

## 8) Controller (Thymeleaf)

```java
package com.seuprojeto.produtos.controller;

import com.seuprojeto.produtos.domain.Produto;
import com.seuprojeto.produtos.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {
    private final ProdutoService service;

    @GetMapping
    public String listar(@RequestParam(value = "q", required = false) String q,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size,
                         @RequestParam(value = "sort", defaultValue = "nome") String sort,
                         @RequestParam(value = "dir", defaultValue = "asc") String dir,
                         Model model){
        Sort s = Sort.by(dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        Pageable pageable = PageRequest.of(page, size, s);
        Page<Produto> pagina = service.listar(q, pageable);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model){
        model.addAttribute("produto", new Produto());
        return "produtos/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("produto") Produto produto,
                         BindingResult br,
                         RedirectAttributes ra){
        if (br.hasErrors()){
            return "produtos/form";
        }
        service.salvar(produto);
        ra.addFlashAttribute("sucesso", "Produto salvo com sucesso!");
        return "redirect:/produtos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra){
        return service.porId(id).map(p -> {
            model.addAttribute("produto", p);
            return "produtos/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("erro", "Produto não encontrado");
            return "redirect:/produtos";
        });
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes ra){
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Produto excluído!");
        return "redirect:/produtos";
    }
}
```

---

## 9) Fragmento base com Bootstrap (`templates/fragments/_base.html`)

```html
<!doctype html>
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title th:replace="~{::title}">Produtos</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link th:href="@{/css/app.css}" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark bg-dark mb-4">
  <div class="container"><a class="navbar-brand" th:href="@{/produtos}">Produtos</a></div>
</nav>
<main class="container" th:insert="~{::section}"></main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script th:src="@{/js/app.js}"></script>
</body>
</html>
```

---

## 10) Lista (`templates/produtos/lista.html`)

```html
<!doctype html>
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org" th:replace="fragments/_base :: html">
<head>
  <title>Produtos</title>
</head>
<section>
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3">Produtos</h1>
    <a class="btn btn-primary" th:href="@{/produtos/novo}">Novo</a>
  </div>

  <form class="row g-2 mb-3" method="get">
    <div class="col-auto">
      <input type="text" class="form-control" name="q" th:value="${q}" placeholder="Buscar por nome...">
    </div>
    <div class="col-auto">
      <button class="btn btn-outline-secondary" type="submit">Buscar</button>
    </div>
  </form>

  <div th:if="${sucesso}" class="alert alert-success" th:text="${sucesso}"></div>
  <div th:if="${erro}" class="alert alert-danger" th:text="${erro}"></div>

  <table class="table table-striped align-middle">
    <thead>
      <tr>
        <th><a th:href="@{|/produtos?q=${q}&sort=nome&dir=${dir=='asc'?'desc':'asc'}|}">Nome</a></th>
        <th>Preço</th>
        <th>Estoque</th>
        <th>Ativo</th>
        <th style="width: 160px">Ações</th>
      </tr>
    </thead>
    <tbody>
      <tr th:each="p : ${pagina.content}">
        <td th:text="${p.nome}"></td>
        <td th:text="${#numbers.formatDecimal(p.preco, 1, 'POINT', 2, 'COMMA')}"></td>
        <td th:text="${p.estoque}"></td>
        <td>
          <span th:classappend="${p.ativo} ? 'badge bg-success' : 'badge bg-secondary'" th:text="${p.ativo} ? 'Sim' : 'Não'"></span>
        </td>
        <td>
          <a class="btn btn-sm btn-outline-primary" th:href="@{|/produtos/editar/${p.id}|}">Editar</a>
          <form th:action="@{|/produtos/excluir/${p.id}|}" method="post" class="d-inline" onsubmit="return confirm('Confirma a exclusão?');">
            <button class="btn btn-sm btn-outline-danger" type="submit">Excluir</button>
          </form>
        </td>
      </tr>
    </tbody>
  </table>

  <nav th:if="${pagina.totalPages > 1}">
    <ul class="pagination">
      <li class="page-item" th:classappend="${pagina.first} ? 'disabled'">
        <a class="page-link" th:href="@{|/produtos?page=${pagina.number-1}&q=${q}|}">Anterior</a>
      </li>
      <li class="page-item" th:each="i : ${#numbers.sequence(0, pagina.totalPages-1)}" th:classappend="${i==pagina.number}? 'active'">
        <a class="page-link" th:text="${i+1}" th:href="@{|/produtos?page=${i}&q=${q}|}"></a>
      </li>
      <li class="page-item" th:classappend="${pagina.last} ? 'disabled'">
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
<html lang="pt-br" xmlns:th="http://www.thymeleaf.org" th:replace="fragments/_base :: html">
<head>
  <title th:text="${produto.id} != null ? 'Editar Produto' : 'Novo Produto'"></title>
</head>
<section>
  <h1 class="h3 mb-3" th:text="${produto.id} != null ? 'Editar Produto' : 'Novo Produto'"></h1>
  <form th:action="@{/produtos}" th:object="${produto}" method="post" class="row g-3">
    <input type="hidden" th:if="${produto.id}" th:field="*{id}">

    <div class="col-md-6">
      <label class="form-label">Nome</label>
      <input class="form-control" th:field="*{nome}">
      <div class="text-danger" th:if="${#fields.hasErrors('nome')}" th:errors="*{nome}"></div>
    </div>

    <div class="col-md-6">
      <label class="form-label">Preço</label>
      <input type="number" step="0.01" class="form-control" th:field="*{preco}">
      <div class="text-danger" th:if="${#fields.hasErrors('preco')}" th:errors="*{preco}"></div>
    </div>

    <div class="col-md-6">
      <label class="form-label">Estoque</label>
      <input type="number" class="form-control" th:field="*{estoque}">
      <div class="text-danger" th:if="${#fields.hasErrors('estoque')}" th:errors="*{estoque}"></div>
    </div>

    <div class="col-md-6">
      <label class="form-label">Ativo</label>
      <select class="form-select" th:field="*{ativo}">
        <option th:value="true">Sim</option>
        <option th:value="false">Não</option>
      </select>
    </div>

    <div class="col-12">
      <label class="form-label">Descrição</label>
      <textarea class="form-control" th:field="*{descricao}" rows="3"></textarea>
    </div>

    <div class="col-12 d-flex gap-2">
      <button class="btn btn-primary" type="submit">Salvar</button>
      <a class="btn btn-outline-secondary" th:href="@{/produtos}">Cancelar</a>
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
package com.seuprojeto.produtos;

import com.seuprojeto.produtos.domain.Produto;
import com.seuprojeto.produtos.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class BootstrapData {
    @Bean
    CommandLineRunner seed(ProdutoRepository repo){
        return args -> {
            if(repo.count()==0){
                repo.save(Produto.builder().nome("Caneta Azul").preco(new BigDecimal("3.50")).estoque(100).ativo(true).descricao("Clássica").build());
                repo.save(Produto.builder().nome("Caderno 100fl").preco(new BigDecimal("15.90")).estoque(50).ativo(true).build());
                repo.save(Produto.builder().nome("Mochila").preco(new BigDecimal("120.00")).estoque(10).ativo(true).build());
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

**Pronto!** Esse esqueleto já atende a maioria dos cenários em sala e projetos iniciais. Se quiser, adapto pro seu repositório e deixo com seed, script SQL e README caprichado. 💙

---

## 19) **README.md** — pronto para colar no seu repositório 👇

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
git clone <URL_DO_REPO>
cd <PASTA_DO_REPO>
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

> Dica: substitua `<URL_DO_REPO>` no README pelo link do seu GitHub.

```
