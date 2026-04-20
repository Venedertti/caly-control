package com.calycontrol.otica.domain.receita;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceitaOpticaService {

    private final ReceitaOpticaRepository repository;

    public List<ReceitaOptica> findAll() {
        return repository.findAllComDetalhes();
    }

    public List<ReceitaOptica> findByCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    public ReceitaOptica findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Receita não encontrada: " + id));
    }

    @Transactional
    public ReceitaOptica save(ReceitaOptica receita) {
        return repository.save(receita);
    }

    @Transactional
    public void delete(Long id) {
        ReceitaOptica receita = findById(id);
        repository.delete(receita);
    }
}
