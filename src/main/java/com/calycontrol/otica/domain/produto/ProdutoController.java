package com.calycontrol.otica.domain.produto;

import com.calycontrol.otica.domain.fornecedor.FornecedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;
    private final FornecedorService fornecedorService;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("produtos", service.buscar(busca));
        model.addAttribute("busca", busca);
        return "produto/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("tipos", TipoProduto.values());
        model.addAttribute("fornecedores", fornecedorService.findAll());
        return "produto/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Produto produto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("tipos", TipoProduto.values());
            model.addAttribute("fornecedores", fornecedorService.findAll());
            return "produto/form";
        }
        service.save(produto);
        redirectAttributes.addFlashAttribute("sucesso", "Produto salvo com sucesso.");
        return "redirect:/produtos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("produto", service.findById(id));
        model.addAttribute("tipos", TipoProduto.values());
        model.addAttribute("fornecedores", fornecedorService.findAll());
        return "produto/form";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Produto removido.");
        return "redirect:/produtos";
    }
}
