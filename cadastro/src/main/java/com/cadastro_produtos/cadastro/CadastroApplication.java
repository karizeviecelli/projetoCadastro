package com.cadastro_produtos.cadastro;

// Importação da classe SpringApplication, usada para iniciar a aplicação Spring Boot
import org.springframework.boot.SpringApplication;
// Importação da anotação @SpringBootApplication, que configura a aplicação como uma aplicação Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Anotação que marca esta classe como a classe principal da aplicação Spring Boot
// @SpringBootApplication combina três anotações:
// - @Configuration: Permite registrar beans no contexto da aplicação.
// - @EnableAutoConfiguration: Habilita a configuração automática do Spring Boot.
// - @ComponentScan: Habilita a varredura de componentes no pacote base e seus subpacotes.
@SpringBootApplication
public class CadastroApplication {

    // Método principal (main) que serve como ponto de entrada da aplicação.
    // O método chama SpringApplication.run para iniciar o contexto da aplicação Spring.
    public static void main(String[] args) {
        SpringApplication.run(CadastroApplication.class, args);
    }

}
