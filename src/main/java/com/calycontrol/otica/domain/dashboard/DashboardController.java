package com.calycontrol.otica.domain.dashboard;

import com.calycontrol.otica.domain.os.OrdemServicoService;
import com.calycontrol.otica.domain.os.StatusOS;
import com.calycontrol.otica.domain.produto.ProdutoService;
import com.calycontrol.otica.domain.venda.Venda;
import com.calycontrol.otica.domain.venda.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class DashboardController {

    private final ProdutoService      produtoService;
    private final VendaService        vendaService;
    private final OrdemServicoService osService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("produtosEstoqueBaixo", produtoService.findEstoqueBaixo());

        List<Venda> ultimasVendas = vendaService.findUltimas20();
        model.addAttribute("ultimasVendas", ultimasVendas);

        // Total e contagem de vendas do dia
        LocalDate hoje = LocalDate.now();
        List<Venda> vendasHoje = ultimasVendas.stream()
                .filter(v -> v.getDataVenda().toLocalDate().equals(hoje))
                .toList();
        BigDecimal totalHoje = vendasHoje.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("vendasHojeCount", vendasHoje.size());
        model.addAttribute("vendasHojeTotal", totalHoje);

        // OS
        model.addAttribute("osAbertasCount", osService.countAbertasEmProducao());
        model.addAttribute("osProntasCount", osService.countProntas());
        model.addAttribute("ultimasOs",      osService.findUltimas10());

        return "dashboard/index";
    }
}
