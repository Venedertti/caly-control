package com.calycontrol.otica.domain.receita;

import com.calycontrol.otica.domain.cliente.ClienteService;
import com.calycontrol.otica.domain.medico.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/receitas")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'TECNICO')")
@RequiredArgsConstructor
public class ReceitaOpticaController {

    private final ReceitaOpticaService service;
    private final ClienteService       clienteService;
    private final MedicoService        medicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("receitas", service.findAll());
        return "receita/lista";
    }

    @GetMapping("/nova")
    public String nova(@RequestParam(required = false) Long clienteId, Model model) {
        ReceitaOptica receita = new ReceitaOptica();
        if (clienteId != null) {
            receita.setCliente(clienteService.findById(clienteId));
        }
        model.addAttribute("receita", receita);
        popularForm(model);
        return "receita/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String salvar(@Valid @ModelAttribute ReceitaOptica receita,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            popularForm(model);
            return "receita/form";
        }
        // Resolve associações pelos IDs submetidos pelo form
        if (receita.getCliente() != null && receita.getCliente().getId() != null) {
            receita.setCliente(clienteService.findById(receita.getCliente().getId()));
        }
        if (receita.getMedico() != null && receita.getMedico().getId() != null) {
            receita.setMedico(medicoService.findById(receita.getMedico().getId()));
        }
        service.save(receita);
        redirectAttributes.addFlashAttribute("sucesso", "Receita salva com sucesso.");
        return "redirect:/receitas";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("receita", service.findById(id));
        return "receita/detalhe";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("receita", service.findById(id));
        popularForm(model);
        return "receita/form";
    }

    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("sucesso", "Receita excluída.");
        return "redirect:/receitas";
    }

    private void popularForm(Model model) {
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("medicos",  medicoService.findAll());
    }
}
