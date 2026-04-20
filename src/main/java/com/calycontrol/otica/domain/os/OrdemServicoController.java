package com.calycontrol.otica.domain.os;

import com.calycontrol.otica.domain.cliente.ClienteService;
import com.calycontrol.otica.domain.produto.Produto;
import com.calycontrol.otica.domain.produto.ProdutoService;
import com.calycontrol.otica.domain.receita.ReceitaOpticaService;
import com.calycontrol.otica.domain.usuario.Usuario;
import com.calycontrol.otica.domain.usuario.UsuarioRepository;
import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/os")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'TECNICO')")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService    osService;
    private final ClienteService         clienteService;
    private final ProdutoService         produtoService;
    private final ReceitaOpticaService   receitaService;
    private final UsuarioRepository      usuarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordens", osService.findAll());
        return "os/lista";
    }

    @GetMapping("/nova")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String nova(@RequestParam(required = false) Long clienteId, Model model) {
        OsForm form = new OsForm();
        if (clienteId != null) form.setClienteId(clienteId);
        model.addAttribute("form", form);
        popularForm(model);
        return "os/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String abrir(@ModelAttribute("form") OsForm form,
                        Authentication auth,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        if (form.getItens().isEmpty()) {
            model.addAttribute("erro", "Adicione pelo menos um produto ou serviço.");
            popularForm(model);
            return "os/form";
        }

        try {
            Usuario operador = usuarioRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new BusinessException("Operador não encontrado."));

            OrdemServico os = new OrdemServico();
            os.setCliente(clienteService.findById(form.getClienteId()));
            os.setUsuario(operador);
            os.setDataPrevisao(form.getDataPrevisao());
            os.setValorPago(form.getValorPago() != null ? form.getValorPago() : BigDecimal.ZERO);
            os.setObservacoes(form.getObservacoes());

            if (form.getReceitaId() != null) {
                os.setReceita(receitaService.findById(form.getReceitaId()));
            }

            Map<Long, Produto> produtos = produtoService.findAll()
                    .stream().collect(Collectors.toMap(Produto::getId, p -> p));

            for (ItemOsForm itemForm : form.getItens()) {
                Produto produto = produtos.get(itemForm.getProdutoId());
                if (produto == null) {
                    model.addAttribute("erro", "Produto inválido na lista de itens.");
                    popularForm(model);
                    return "os/form";
                }
                OsProduto item = new OsProduto();
                item.setProduto(produto);
                item.setQuantidade(itemForm.getQuantidade());
                item.setPrecoUnitario(itemForm.getPrecoUnitario());
                item.setDesconto(itemForm.getDesconto() != null ? itemForm.getDesconto()
                                                                 : BigDecimal.ZERO);
                os.getItens().add(item);
            }

            osService.abrir(os);
            redirectAttributes.addFlashAttribute("sucesso", "OS " + os.getNumeroOs() + " aberta com sucesso.");
            return "redirect:/os";

        } catch (BusinessException e) {
            model.addAttribute("erro", e.getMessage());
            popularForm(model);
            return "os/form";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("os", osService.findById(id));
        return "os/detalhe";
    }

    @PostMapping("/{id}/avancar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'TECNICO')")
    public String avancar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            osService.avancarStatus(id);
            redirectAttributes.addFlashAttribute("sucesso", "Status da OS atualizado.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/os/" + id;
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            osService.cancelar(id);
            redirectAttributes.addFlashAttribute("sucesso", "OS cancelada.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/os/" + id;
    }

    private void popularForm(Model model) {
        model.addAttribute("clientes",  clienteService.findAll());
        model.addAttribute("produtos",  produtoService.findAll());
        model.addAttribute("receitas",  receitaService.findAll());
    }
}
