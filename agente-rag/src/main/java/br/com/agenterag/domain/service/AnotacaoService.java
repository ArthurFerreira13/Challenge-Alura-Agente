package br.com.agenterag.domain.service;


import br.com.agenterag.domain.dto.AnotacaoResponse;
import br.com.agenterag.domain.dto.SalvarAnotacaoRequest;
import br.com.agenterag.domain.internal.AnotacaoQuestao;
import br.com.agenterag.domain.internal.AnotacaoQuestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnotacaoService {

    private final AnotacaoQuestaoRepository repository;

    public AnotacaoService(AnotacaoQuestaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AnotacaoResponse salvarOuAtualizar(SalvarAnotacaoRequest request) {
        AnotacaoQuestao anotacao = repository
                .findByUsuarioIdAndQuestaoId(request.usuarioId(), request.questaoId())
                .orElseGet(() -> {
                    AnotacaoQuestao nova = new AnotacaoQuestao();
                    nova.setUsuarioId(request.usuarioId());
                    nova.setQuestaoId(request.questaoId());
                    return nova;
                });

        anotacao.setTexto(request.texto());
        AnotacaoQuestao salva = repository.save(anotacao);

        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public Optional<AnotacaoResponse> buscarPorUsuarioEQuestao(Long usuarioId, Long questaoId) {
        return repository.findByUsuarioIdAndQuestaoId(usuarioId, questaoId)
                .map(this::toResponse);
    }

    private AnotacaoResponse toResponse(AnotacaoQuestao anotacao) {
        return new AnotacaoResponse(
                anotacao.getId(),
                anotacao.getUsuarioId(),
                anotacao.getQuestaoId(),
                anotacao.getTexto(),
                anotacao.getAtualizadoEm()
        );
    }
}