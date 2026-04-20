package com.calycontrol.otica.domain.medico;

import com.calycontrol.otica.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository repository;

    public List<Medico> findAll() {
        return repository.findByAtivoTrueOrderByNome();
    }

    public Medico findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Médico não encontrado: " + id));
    }

    @Transactional
    public Medico save(Medico medico) {
        repository.findByCrm(medico.getCrm())
                .filter(m -> !m.getId().equals(medico.getId()))
                .ifPresent(m -> { throw new BusinessException("CRM já cadastrado."); });
        return repository.save(medico);
    }

    @Transactional
    public void desativar(Long id) {
        Medico medico = findById(id);
        medico.setAtivo(false);
        repository.save(medico);
    }
}
