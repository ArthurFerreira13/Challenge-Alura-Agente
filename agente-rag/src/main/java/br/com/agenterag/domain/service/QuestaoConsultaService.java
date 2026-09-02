package br.com.agenterag.domain.service;

import br.com.agenterag.domain.exception.QuestaoNaoEncontradaException;
import br.com.agenterag.domain.internal.Disciplina;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.QuestaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuestaoConsultaService {

    private final QuestaoRepository questaoRepository;

    public QuestaoConsultaService(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    public Page<Questao> listar(Disciplina disciplina, Pageable pageable) {
        return (disciplina != null)
                ? questaoRepository.findByDisciplina(disciplina, pageable)
                : questaoRepository.findAll(pageable);
    }

    public boolean verificarResposta(Long questaoId, String alternativa) {
        Questao questao = buscarPorId(questaoId);
        return alternativa != null && alternativa.equalsIgnoreCase(questao.getAlternativaCorreta());
    }

    public String buscarAlternativaCorreta(Long questaoId) {
        return buscarPorId(questaoId).getAlternativaCorreta();
    }

    private Questao buscarPorId(Long questaoId) {
        return questaoRepository.findById(questaoId)
                .orElseThrow(() -> new QuestaoNaoEncontradaException(questaoId));
    }
}