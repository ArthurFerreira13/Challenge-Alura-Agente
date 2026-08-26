package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.ProvaOab;
import br.com.agenterag.domain.internal.ProvaOabRepository;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.StatusIngestao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimuladoOrchestratorService {
    private final ProvaOabRepository provaRepository;
    private final SimuladoRepository simuladoRepository;
    private final PdfParserService pdfParserService;

    public SimuladoOrchestratorService(
            ProvaOabRepository provaRepository,
            SimuladoRepository simuladoRepository,
            PdfParserService pdfParserService) {
        this.provaRepository = provaRepository;
        this.simuladoRepository = simuladoRepository;
        this.pdfParserService = pdfParserService;
    }

    @Transactional
    public Simulado ingerirEGerarSimulado(String edicao, String nomeArquivo, byte[] pdfBytes) {
        // 1. Salva a prova bruta com byte[]
        ProvaOab prova = new ProvaOab();
        prova.setEdicao(edicao);
        prova.setNomeArquivo(nomeArquivo);
        prova.setConteudoPdf(pdfBytes);
        prova.setStatus(StatusIngestao.PENDENTE);
        prova = provaRepository.save(prova);

        // 2. Extrai as questões do PDF através do Parser
        List<Questao> questoesExtraidas = pdfParserService.extrairQuestoes(pdfBytes);

        // 3. Monta o Objeto Simulado
        Simulado simulado = new Simulado();
        simulado.setTitulo("Simulado OAB - " + edicao);
        simulado.setTempoLimiteMinutos(300);

        for (Questao q : questoesExtraidas) {
            q.setProvaOrigem(prova);
            q.setSimulado(simulado);
        }
        simulado.setQuestoes(questoesExtraidas);

        // 4. Salva o Simulado completo e atualiza o status da prova
        Simulado simuladoSalvo = simuladoRepository.save(simulado);
        prova.setStatus(StatusIngestao.PROCESSADO);
        provaRepository.save(prova);

        return simuladoSalvo;
    }
}
