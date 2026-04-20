package com.calycontrol.otica.domain.fornecedor;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/fornecedores")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", service.findAll());
        return "fornecedor/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedor/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Fornecedor fornecedor,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "fornecedor/form";
        }
        service.save(fornecedor);
        redirectAttributes.addFlashAttribute("sucesso", "Fornecedor salvo com sucesso.");
        return "redirect:/fornecedores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", service.findById(id));
        return "fornecedor/form";
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Fornecedor removido.");
        return "redirect:/fornecedores";
    }
}
