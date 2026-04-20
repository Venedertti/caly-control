package com.calycontrol.otica.domain.venda;

import com.calycontrol.otica.domain.cliente.ClienteService;
import com.calycontrol.otica.domain.produto.Produto;
import com.calycontrol.otica.domain.produto.ProdutoService;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendas")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService        vendaService;
    private final ClienteService      clienteService;
    private final ProdutoService      produtoService;
    private final UsuarioRepository   usuarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vendas", vendaService.findAll());
        return "venda/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        popularForm(model);
        model.addAttribute("form", new VendaForm());
        return "venda/form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("form") VendaForm form,
                            BindingResult result,
                            Authentication auth,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (form.getItens().isEmpty()) {
            result.rejectValue("itens", "itens.vazio", "Adicione pelo menos um produto.");
        }

        if (result.hasErrors()) {
            popularForm(model);
            return "venda/form";
        }

        try {
            Usuario operador = usuarioRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new BusinessException("Operador não encontrado."));

            Venda venda = new Venda();
            venda.setCliente(clienteService.findById(form.getClienteId()));
            venda.setUsuario(operador);
            venda.setFormaPagamento(form.getFormaPagamento());
            venda.setParcelas(form.getParcelas());

            // Monta os itens resolvendo cada produto
            Map<Long, Produto> produtos = produtoService.findAll()
                    .stream().collect(Collectors.toMap(Produto::getId, p -> p));

            for (ItemVendaForm itemForm : form.getItens()) {
                Produto produto = produtos.get(itemForm.getProdutoId());
                if (produto == null) {
                    result.rejectValue("itens", "produto.invalido", "Produto inválido na lista de itens.");
                    popularForm(model);
                    return "venda/form";
                }
                VendaProduto item = new VendaProduto();
                item.setProduto(produto);
                item.setQuantidade(itemForm.getQuantidade());
                item.setPrecoUnitario(itemForm.getPrecoUnitario());
                item.setDesconto(itemForm.getDesconto() != null ? itemForm.getDesconto()
                                                                 : java.math.BigDecimal.ZERO);
                venda.getItens().add(item);
            }

            vendaService.registrar(venda);
            redirectAttributes.addFlashAttribute("sucesso", "Venda registrada com sucesso.");
            return "redirect:/vendas";

        } catch (BusinessException e) {
            model.addAttribute("erro", e.getMessage());
            popularForm(model);
            return "venda/form";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("venda", vendaService.findById(id));
        return "venda/detalhe";
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void popularForm(Model model) {
        model.addAttribute("clientes",       clienteService.findAll());
        model.addAttribute("produtos",       produtoService.findAll());
        model.addAttribute("formasPagamento", FormaPagamento.values());
    }
}
