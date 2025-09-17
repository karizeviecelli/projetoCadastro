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
