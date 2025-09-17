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