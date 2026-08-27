package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.ProvaOab;
import br.com.agenterag.domain.internal.ProvaOabRepository;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.StatusIngestao;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // 1. Salva a prova bruta
        ProvaOab prova = new ProvaOab();
        prova.setEdicao(edicao);
        prova.setNomeArquivo(nomeArquivo);
        prova.setConteudoPdf(pdfProvaBytes);
        prova.setStatus(StatusIngestao.PENDENTE);
        prova = provaRepository.save(prova);

        // 2. Extrai questões da prova e respostas do gabarito, separadamente.
        // tipoProva = 1 fixo: sempre baixar o Caderno Tipo 1 (Branco) tanto
        // da prova quanto do gabarito, evitando a tabela de correspondência.
        List<Questao> questoesExtraidas = pdfParserService.extrairQuestoes(pdfProvaBytes);
        Map<Integer, String> gabarito = gabaritoParser.extrairRespostas(pdfGabaritoBytes, 1);

        // 3. Casa cada questão com sua resposta correta pelo número
        int semGabarito = 0;
        List<Questao> questoesValidas = new ArrayList<>();
        for (Questao q : questoesExtraidas) {
            String correta = gabarito.get(q.getNumeroQuestao());
            if (correta == null || GabaritoParser.ANULADA.equals(correta)) {
                semGabarito++;
                log.warn("Questão {} da prova '{}' sem resposta válida no gabarito ({}) — descartada",
                        q.getNumeroQuestao(), edicao, correta == null ? "não encontrada" : "anulada");
                continue; // não salva questão sem gabarito confirmado, nem questão anulada
            }
            q.setAlternativaCorreta(correta);
            questoesValidas.add(q);
        }

        // 4. Monta o Simulado só com as questões que têm gabarito confiável
        Simulado simulado = new Simulado();
        simulado.setTitulo("Simulado OAB - " + edicao);
        simulado.setTempoLimiteMinutos(300);

        for (Questao q : questoesValidas) {
            q.setProvaOrigem(prova);
            q.setSimulado(simulado);
        }
        simulado.setQuestoes(questoesValidas);

        // 5. Salva o Simulado completo e atualiza o status da prova
        Simulado simuladoSalvo = simuladoRepository.save(simulado);
        prova.setStatus(questoesValidas.isEmpty() ? StatusIngestao.ERRO : StatusIngestao.PROCESSADO);
        provaRepository.save(prova);

        log.info("Prova '{}': {} questões extraídas, {} salvas com gabarito, {} descartadas",
                edicao, questoesExtraidas.size(), questoesValidas.size(), semGabarito);

        return simuladoSalvo;
    }
}
