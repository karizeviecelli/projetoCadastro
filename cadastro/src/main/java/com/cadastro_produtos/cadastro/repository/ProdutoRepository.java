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
