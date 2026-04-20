package com.calycontrol.otica.domain.fornecedor;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository repository;

    public List<Fornecedor> findAll() {
        return repository.findByAtivoTrueOrderByRazaoSocial();
    }

    public Fornecedor findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Fornecedor não encontrado: " + id));
    }

    @Transactional
    public Fornecedor save(Fornecedor fornecedor) {
        if (StringUtils.hasText(fornecedor.getCnpj())) {
            repository.findByCnpj(fornecedor.getCnpj())
                    .filter(f -> !f.getId().equals(fornecedor.getId()))
                    .ifPresent(f -> { throw new BusinessException("CNPJ já cadastrado."); });
        }
        return repository.save(fornecedor);
    }

    @Transactional
    public void desativar(Long id) {
        Fornecedor fornecedor = findById(id);
        fornecedor.setAtivo(false);
        repository.save(fornecedor);
    }
}
