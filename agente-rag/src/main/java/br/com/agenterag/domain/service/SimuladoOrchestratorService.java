package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.ProvaOab;
import br.com.agenterag.domain.internal.ProvaOabRepository;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.StatusIngestao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SimuladoOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(SimuladoOrchestratorService.class);

    private final ProvaOabRepository provaRepository;
    private final SimuladoRepository simuladoRepository;
    private final PdfParserService pdfParserService;
    private final GabaritoParser gabaritoParser;

    public SimuladoOrchestratorService(
            ProvaOabRepository provaRepository,
            SimuladoRepository simuladoRepository,
            PdfParserService pdfParserService,
            GabaritoParser gabaritoParser) {
        this.provaRepository = provaRepository;
        this.simuladoRepository = simuladoRepository;
        this.pdfParserService = pdfParserService;
        this.gabaritoParser = gabaritoParser;
    }

    @Transactional
    public Simulado ingerirEGerarSimulado(String edicao, String nomeArquivo,
                                          byte[] pdfProvaBytes, byte[] pdfGabaritoBytes) {

        Optional<ProvaOab> provaExistente = provaRepository.findByNomeArquivo(nomeArquivo);
        if (provaExistente.isPresent()) {
            ProvaOab jaProcessada = provaExistente.get();
            log.info("Prova '{}' (arquivo '{}') já processada anteriormente (ID {}), ignorando reingestão.",
                    edicao, nomeArquivo, jaProcessada.getId());
            return jaProcessada.getQuestoes().isEmpty()
                    ? null
                    : jaProcessada.getQuestoes().get(0).getSimulado();
        }

        ProvaOab prova = new ProvaOab();
        prova.setEdicao(edicao);
        prova.setNomeArquivo(nomeArquivo);
        prova.setConteudoPdf(pdfProvaBytes);
        prova.setStatus(StatusIngestao.PENDENTE);
        prova = provaRepository.save(prova);

        List<Questao> questoesExtraidas = pdfParserService.extrairQuestoes(pdfProvaBytes);
        Map<Integer, String> gabarito = gabaritoParser.extrairRespostas(pdfGabaritoBytes, 1);

        List<Questao> questoesValidas = new ArrayList<>();
        int descartadas = 0;

        Simulado simulado = new Simulado();
        simulado.setTitulo("Simulado OAB - " + edicao);
        simulado.setTempoLimiteMinutos(300);

        for (Questao q : questoesExtraidas) {
            String correta = gabarito.get(q.getNumeroQuestao());

            if (correta == null || GabaritoParser.ANULADA.equals(correta)) {
                descartadas++;
                log.warn("Questão {} desconsiderada. Gabarito: {}", q.getNumeroQuestao(), correta);
                continue;
            }

            q.setAlternativaCorreta(correta);
            q.setDisciplina(DisciplinaMapper.paraQuestao(q.getNumeroQuestao()));
            q.setProvaOrigem(prova);
            q.setSimulado(simulado);

            // Atualiza a chave estrangeira do relacionamento filho nas alternativas
            if (q.getAlternativas() != null) {
                q.getAlternativas().forEach(alt -> alt.setQuestao(q));
            }

            questoesValidas.add(q);
        }

        simulado.setQuestoes(questoesValidas);

        Simulado simuladoSalvo = simuladoRepository.save(simulado);

        prova.setStatus(questoesValidas.isEmpty() ? StatusIngestao.ERRO : StatusIngestao.PROCESSADO);
        provaRepository.save(prova);

        log.info("Processamento concluído. Prova: '{}' | Processadas: {} | Salvas: {} | Ignoradas: {}",
                edicao, questoesExtraidas.size(), questoesValidas.size(), descartadas);

        return simuladoSalvo;
    }
}