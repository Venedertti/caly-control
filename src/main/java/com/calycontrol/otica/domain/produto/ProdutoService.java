package com.calycontrol.otica.domain.produto;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    public List<Produto> findAll() {
        return repository.findByAtivoTrueOrderByDescricao();
    }

    public List<Produto> buscar(String termo) {
        if (!StringUtils.hasText(termo)) {
            return findAll();
        }
        return repository.buscar(termo.trim());
    }

    public List<Produto> findEstoqueBaixo() {
        return repository.findEstoqueBaixo();
    }

    public Produto findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto save(Produto produto) {
        repository.findByCodigo(produto.getCodigo())
                .filter(p -> !p.getId().equals(produto.getId()))
                .ifPresent(p -> { throw new BusinessException("Código de produto já cadastrado."); });
        return repository.save(produto);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = findById(id);
        produto.setAtivo(false);
        repository.save(produto);
    }
}
