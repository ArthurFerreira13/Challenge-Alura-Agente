package br.com.agenterag.domain.service;

import br.com.agenterag.domain.exception.SessaoJaFinalizadaException;
import br.com.agenterag.domain.exception.SessaoNaoEncontradaException;
import br.com.agenterag.domain.internal.DesempenhoPorDisciplina;
import br.com.agenterag.domain.internal.Disciplina;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.RespostaUsuario;
import br.com.agenterag.domain.internal.RespostaUsuarioRepository;
import br.com.agenterag.domain.internal.ResultadoSimulado;
import br.com.agenterag.domain.internal.ResultadoSimuladoRepository;
import br.com.agenterag.domain.internal.SimuladoSessao;
import br.com.agenterag.domain.internal.SimuladoSessaoRepository;
import br.com.agenterag.domain.internal.StatusSessao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApuracaoResultadoService {

    // TODO: confirmar o percentual de corte real do Exame Unificado FGV
    private static final double PERCENTUAL_APROVACAO = 0.40;

    private final SimuladoSessaoRepository sessaoRepository;
    private final RespostaUsuarioRepository respostaRepository;
    private final ResultadoSimuladoRepository resultadoRepository;
    private final Clock clock;

    public ApuracaoResultadoService(SimuladoSessaoRepository sessaoRepository,
                                    RespostaUsuarioRepository respostaRepository,
                                    ResultadoSimuladoRepository resultadoRepository,
                                    Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.respostaRepository = respostaRepository;
        this.resultadoRepository = resultadoRepository;
        this.clock = clock;
    }

    @Transactional
    public ResultadoSimulado apurar(Long sessaoId) {
        SimuladoSessao sessao = sessaoRepository.findByIdParaAtualizar(sessaoId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(sessaoId));

        if (sessao.getStatus() == StatusSessao.CONCLUIDO) {
            throw new SessaoJaFinalizadaException(sessaoId);
        }

        List<RespostaUsuario> respostas = respostaRepository.findBySessaoId(sessaoId);
        List<Questao> questoesDoSimulado = sessao.getSimulado().getQuestoes();

        Map<Long, RespostaUsuario> respostaPorQuestaoId = respostas.stream()
                .collect(Collectors.toMap(r -> r.getQuestao().getId(), r -> r));

        List<ResultadoQuestao> resultadosPorQuestao = questoesDoSimulado.stream()
                .map(questao -> avaliar(questao, respostaPorQuestaoId.get(questao.getId())))
                .toList();

        long acertos = resultadosPorQuestao.stream().filter(ResultadoQuestao::acertou).count();
        long emBranco = resultadosPorQuestao.stream().filter(r -> !r.respondida()).count();
        long erros = resultadosPorQuestao.size() - acertos - emBranco;

        int totalQuestoes = questoesDoSimulado.size();
        double percentual = totalQuestoes == 0 ? 0.0 : (double) acertos / totalQuestoes;

        ResultadoSimulado resultado = new ResultadoSimulado();
        resultado.setSessao(sessao);
        resultado.setTotalAcertos((int) acertos);
        resultado.setTotalErros((int) erros);
        resultado.setTotalEmBranco((int) emBranco);
        resultado.setPercentualAcerto(percentual);
        resultado.setAprovado(percentual >= PERCENTUAL_APROVACAO);
        resultado.setTempoGastoSegundos(Duration.between(sessao.getIniciadoEm(), LocalDateTime.now(clock)).getSeconds());
        resultado.setGeradoEm(LocalDateTime.now(clock));

        List<DesempenhoPorDisciplina> desempenhos = resultadosPorQuestao.stream()
                .collect(Collectors.groupingBy(ResultadoQuestao::disciplina))
                .entrySet().stream()
                .map(entry -> paraDesempenho(resultado, entry.getKey(), entry.getValue()))
                .toList();
        resultado.getDesempenhoPorDisciplina().addAll(desempenhos);

        sessao.setStatus(StatusSessao.CONCLUIDO);
        sessao.setConcluidoEm(LocalDateTime.now(clock));
        sessaoRepository.save(sessao);

        try {
            return resultadoRepository.save(resultado);
        } catch (DataIntegrityViolationException e) {
            throw new SessaoJaFinalizadaException(sessaoId);
        }
    }

    private ResultadoQuestao avaliar(Questao questao, RespostaUsuario resposta) {
        String marcada = (resposta != null) ? resposta.getAlternativaEscolhida() : null;
        boolean respondida = marcada != null && !marcada.isBlank();
        boolean acertou = respondida && marcada.equalsIgnoreCase(questao.getAlternativaCorreta());
        return new ResultadoQuestao(questao.getDisciplina(), respondida, acertou);
    }

    private DesempenhoPorDisciplina paraDesempenho(ResultadoSimulado resultado, Disciplina disciplina,
                                                   List<ResultadoQuestao> resultadosDaDisciplina) {
        DesempenhoPorDisciplina desempenho = new DesempenhoPorDisciplina();
        desempenho.setResultado(resultado);
        desempenho.setDisciplina(disciplina);
        desempenho.setAcertos((int) resultadosDaDisciplina.stream().filter(ResultadoQuestao::acertou).count());
        desempenho.setTotal(resultadosDaDisciplina.size());
        return desempenho;
    }

    private record ResultadoQuestao(Disciplina disciplina, boolean respondida, boolean acertou) {
    }
}