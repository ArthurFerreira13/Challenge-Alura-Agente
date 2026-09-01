package br.com.agenterag.domain.service;

import br.com.agenterag.domain.exception.QuestaoNaoPertenceAoSimuladoException;
import br.com.agenterag.domain.exception.SessaoExpiradaException;
import br.com.agenterag.domain.exception.SessaoJaFinalizadaException;
import br.com.agenterag.domain.exception.SessaoNaoEncontradaException;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.RespostaUsuario;
import br.com.agenterag.domain.internal.RespostaUsuarioRepository;
import br.com.agenterag.domain.internal.SimuladoSessao;
import br.com.agenterag.domain.internal.SimuladoSessaoRepository;
import br.com.agenterag.domain.internal.StatusSessao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RespostaSimuladoService {

    private final SimuladoSessaoRepository sessaoRepository;
    private final RespostaUsuarioRepository respostaRepository;
    private final Clock clock;

    public RespostaSimuladoService(SimuladoSessaoRepository sessaoRepository,
                                   RespostaUsuarioRepository respostaRepository,
                                   Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.respostaRepository = respostaRepository;
        this.clock = clock;
    }

    @Transactional
    public void responder(Long sessaoId, Long questaoId, String alternativa) {
        // mesmo lock pessimista usado em ApuracaoResultadoService.apurar() —
        // evita responder() correr ao mesmo tempo que a sessão está sendo finalizada
        SimuladoSessao sessao = sessaoRepository.findByIdParaAtualizar(sessaoId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(sessaoId));

        if (sessao.getStatus() != StatusSessao.EM_ANDAMENTO) {
            throw new SessaoJaFinalizadaException(sessaoId);
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        if (sessao.isExpirada(agora)) {
            sessao.setStatus(StatusSessao.EXPIRADA);
            sessaoRepository.save(sessao);
            throw new SessaoExpiradaException(sessaoId);
        }

        Questao questao = sessao.getSimulado().getQuestoes().stream()
                .filter(q -> q.getId().equals(questaoId))
                .findFirst()
                .orElseThrow(() -> new QuestaoNaoPertenceAoSimuladoException(questaoId, sessao.getSimulado().getId()));

        RespostaUsuario resposta = respostaRepository
                .findBySessaoIdAndQuestaoId(sessaoId, questaoId)
                .orElseGet(RespostaUsuario::new);

        resposta.setSessao(sessao);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);
        resposta.setRespondidoEm(LocalDateTime.now(clock));

        respostaRepository.save(resposta);

        sessao.setIndiceAtual(sessao.getIndiceAtual() + 1);
        sessaoRepository.save(sessao);
    }
}