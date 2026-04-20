package com.calycontrol.otica.domain.medico;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("medicos", service.findAll());
        return "medico/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("medico", new Medico());
        return "medico/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Medico medico,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "medico/form";
        }
        service.save(medico);
        redirectAttributes.addFlashAttribute("sucesso", "Médico salvo com sucesso.");
        return "redirect:/medicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("medico", service.findById(id));
        return "medico/form";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Médico removido.");
        return "redirect:/medicos";
    }
}
