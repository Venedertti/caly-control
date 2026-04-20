package com.calycontrol.otica.domain.cliente;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("clientes", service.buscar(busca));
        model.addAttribute("busca", busca);
        return "cliente/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cliente/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Cliente cliente,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/form";
        }
        service.save(cliente);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente salvo com sucesso.");
        return "redirect:/clientes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.findById(id));
        return "cliente/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.findById(id));
        return "cliente/form";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Cliente removido.");
        return "redirect:/clientes";
    }

    /**
     * LGPD Art. 18, IV — direito à eliminação.
     * Restrito a ADMIN por ser ação irreversível sobre dados pessoais.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/anonimizar")
    public String anonimizar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.anonimizar(id);
        redirectAttributes.addFlashAttribute("sucesso",
                "Dados pessoais do cliente anonimizados (LGPD Art. 18, IV).");
        return "redirect:/clientes";
    }
}
