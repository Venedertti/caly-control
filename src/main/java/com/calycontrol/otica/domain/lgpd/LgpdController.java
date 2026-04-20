package com.calycontrol.otica.domain.lgpd;

import com.calycontrol.otica.domain.cliente.Cliente;
import com.calycontrol.otica.domain.cliente.ClienteService;
import com.calycontrol.otica.domain.historico.HistoricoTransacao;
import com.calycontrol.otica.domain.historico.HistoricoTransacaoRepository;
import com.calycontrol.otica.domain.receita.ReceitaOptica;
import com.calycontrol.otica.domain.receita.ReceitaOpticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposições HTTP para atendimento dos direitos do titular (LGPD Art. 18):
 *   - Art. 18, II  — confirmação da existência de tratamento.
 *   - Art. 18, V   — portabilidade dos dados em formato estruturado (JSON).
 *
 * Acesso restrito a ADMIN. O atendimento deve ser formalizado por solicitação
 * do titular; este endpoint é a ferramenta operacional usada pelo encarregado.
 */
@RestController
@RequestMapping("/lgpd")
@RequiredArgsConstructor
public class LgpdController {

    private final ClienteService clienteService;
    private final ReceitaOpticaRepository receitaRepository;
    private final HistoricoTransacaoRepository historicoRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/clientes/{id}/meus-dados", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exportarDadosDoTitular(@PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("formato", "application/json");
        dados.put("baseLegal", "LGPD Art. 18, V — portabilidade dos dados");
        dados.put("cliente", montarCliente(cliente));
        dados.put("receitas", montarReceitas(receitaRepository.findByClienteId(id)));
        dados.put("transacoes", montarHistorico(historicoRepository.findByClienteId(id)));

        String filename = "dados-titular-" + id + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(dados);
    }

    private Map<String, Object> montarCliente(Cliente c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("nome", c.getNome());
        m.put("cpf", c.getCpf());
        m.put("telefone", c.getTelefone());
        m.put("email", c.getEmail());
        m.put("cep", c.getCep());
        m.put("endereco", c.getEndereco());
        m.put("dataNascimento", c.getDataNascimento());
        m.put("ativo", c.isAtivo());
        m.put("anonimizado", c.isAnonimizado());
        m.put("dataAnonimizacao", c.getDataAnonimizacao());
        return m;
    }

    private List<Map<String, Object>> montarReceitas(List<ReceitaOptica> receitas) {
        return receitas.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("dataEmissao", r.getDataEmissao());
            m.put("validade", r.getValidade());
            m.put("medico", r.getMedico().getNome());
            m.put("crm", r.getMedico().getCrm());
            m.put("odEsf", r.getOdEsf());
            m.put("odCil", r.getOdCil());
            m.put("odEixo", r.getOdEixo());
            m.put("oeEsf", r.getOeEsf());
            m.put("oeCil", r.getOeCil());
            m.put("oeEixo", r.getOeEixo());
            m.put("adicao", r.getAdicao());
            m.put("observacoes", r.getObservacoes());
            m.put("baseLegal", r.getBaseLegal());
            return m;
        }).toList();
    }

    private List<Map<String, Object>> montarHistorico(List<HistoricoTransacao> historico) {
        return historico.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tipo", h.getTipo());
            m.put("subtipo", h.getSubtipo());
            m.put("numero", h.getNumeroReferencia());
            m.put("data", h.getDataTransacao());
            m.put("valorTotal", h.getValorTotal());
            m.put("formaPagamento", h.getFormaPagamento());
            m.put("itensResumo", h.getItensResumo());
            return m;
        }).toList();
    }
}
