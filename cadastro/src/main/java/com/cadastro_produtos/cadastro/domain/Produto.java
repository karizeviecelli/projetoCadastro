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
