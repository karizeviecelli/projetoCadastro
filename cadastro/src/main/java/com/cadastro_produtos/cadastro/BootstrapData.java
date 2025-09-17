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