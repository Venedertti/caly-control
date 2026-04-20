package com.calycontrol.otica.domain.os;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;

    private static final DateTimeFormatter NUM_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<OrdemServico> findAll() {
        return repository.findAllComDetalhes();
    }

    public OrdemServico findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Ordem de Serviço não encontrada: " + id));
    }

    public long countAbertasEmProducao() {
        return repository.countByStatus(StatusOS.ABERTA)
             + repository.countByStatus(StatusOS.EM_PRODUCAO);
    }

    public long countProntas() {
        return repository.countByStatus(StatusOS.PRONTA);
    }

    public List<OrdemServico> findUltimas10() {
        return repository.findTop10ByOrderByIdDesc();
    }

    @Transactional
    public OrdemServico abrir(OrdemServico os) {
        if (os.getItens().isEmpty()) {
            throw new BusinessException("A OS deve ter pelo menos um produto ou serviço.");
        }

        os.setNumeroOs(gerarNumero());
        os.getItens().forEach(item -> item.setOs(os));
        os.recalcularTotal();

        return repository.save(os);
    }

    @Transactional
    public void avancarStatus(Long id) {
        OrdemServico os = findById(id);
        if (!os.podeAvancar()) {
            throw new BusinessException("OS não pode ser avançada no status atual: " + os.getStatus().getDescricao());
        }
        os.avancar();
        repository.save(os);
    }

    @Transactional
    public void cancelar(Long id) {
        OrdemServico os = findById(id);
        if (!os.podeCancelar()) {
            throw new BusinessException("OS não pode ser cancelada no status atual: " + os.getStatus().getDescricao());
        }
        os.setStatus(StatusOS.CANCELADA);
        repository.save(os);
    }

    private String gerarNumero() {
        String numero;
        int tentativas = 0;
        do {
            long seq = repository.count() + 1 + tentativas;
            String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            numero = String.format("OS-%s-%05d", data, seq);
            tentativas++;
            if (tentativas > 20) {
                throw new BusinessException("Não foi possível gerar número único para a OS.");
            }
        } while (repository.existsByNumeroOs(numero));
        return numero;
    }
}
