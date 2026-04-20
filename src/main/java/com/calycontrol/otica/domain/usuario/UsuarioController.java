package com.calycontrol.otica.domain.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", service.findAll());
        return "usuario/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", PerfilUsuario.values());
        return "usuario/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", PerfilUsuario.values());
            return "usuario/form";
        }
        service.save(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário salvo com sucesso.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", service.findById(id));
        model.addAttribute("perfis", PerfilUsuario.values());
        return "usuario/form";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário desativado.");
        return "redirect:/usuarios";
    }
}
