package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.Alternativa;
import br.com.agenterag.domain.internal.ProvaOab;
import br.com.agenterag.domain.internal.ProvaOabRepository;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.internal.StatusIngestao;
import br.com.agenterag.ingestion.dto.QuestaoExtracted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        // Verifica se já existe uma prova com este nome de arquivo
        Optional<ProvaOab> provaExistente = provaRepository.findByNomeArquivo(nomeArquivo);
        if (provaExistente.isPresent()) {
            ProvaOab jaProcessada = provaExistente.get();

            if (!jaProcessada.getQuestoes().isEmpty()) {
                log.info("Prova '{}' (arquivo '{}') já processada com sucesso anteriormente (ID {}), ignorando reingestão.",
                        edicao, nomeArquivo, jaProcessada.getId());
                return jaProcessada.getQuestoes().get(0).getSimulado();
            }

            // Registro existe mas ficou sem questões válidas – remova e reingira
            log.warn("Prova '{}' (arquivo '{}') existia sem questões válidas (ID {}, status {}). Removendo e reingerindo do zero.",
                    edicao, nomeArquivo, jaProcessada.getId(), jaProcessada.getStatus());
            provaRepository.delete(jaProcessada);
            provaRepository.flush();
        }

        // --- CRIA O REGISTRO DA PROVA COM O ANO ---
        ProvaOab prova = new ProvaOab();
        prova.setEdicao(edicao);
        prova.setNomeArquivo(nomeArquivo);
        prova.setConteudoPdf(pdfProvaBytes);
        prova.setStatus(StatusIngestao.PENDENTE);
        prova.setCriadoEm(LocalDateTime.now());
        prova.setAno(extrairAnoDaEdicao(edicao));  // <--- AQUI É O AJUSTE
        prova = provaRepository.save(prova);

        // --- EXTRAI QUESTÕES USANDO A ESTRATÉGIA DE PARSING ---
        List<QuestaoExtracted> questoesExtraidasDto;
        try {
            questoesExtraidasDto = pdfParserService.extrairQuestoes(pdfProvaBytes, nomeArquivo, null);
        } catch (Exception e) {
            log.error("Falha na extração de questões do PDF '{}'", nomeArquivo, e);
            prova.setStatus(StatusIngestao.ERRO);
            provaRepository.save(prova);
            throw new RuntimeException("Erro ao extrair questões: " + e.getMessage(), e);
        }

        // --- EXTRAI GABARITO ---
        Map<Integer, String> gabarito = gabaritoParser.extrairRespostas(pdfGabaritoBytes, 1);

        // --- CONVERTE DTOs PARA ENTIDADES, EVITANDO DUPLICATAS ---
        List<Questao> questoesValidas = new ArrayList<>();
        int descartadas = 0;
        Set<Integer> numerosJaProcessados = new HashSet<>();

        Simulado simulado = new Simulado();
        simulado.setTitulo("Simulado OAB - " + edicao);
        simulado.setTempoLimiteMinutos(300);

        for (QuestaoExtracted dto : questoesExtraidasDto) {
            Integer numero = dto.numero();
            String correta = gabarito.get(numero);

            // Verifica se a questão já foi processada (evita duplicatas)
            if (numerosJaProcessados.contains(numero)) {
                log.warn("Questão {} duplicada, ignorando.", numero);
                continue;
            }

            // Verifica se o gabarito existe e não é anulado
            if (correta == null || GabaritoParser.ANULADA.equals(correta)) {
                descartadas++;
                log.warn("Questão {} desconsiderada. Gabarito: {}", numero, correta);
                continue;
            }

            // Converte DTO para entidade Questao
            Questao questao = new Questao();
            questao.setNumeroQuestao(numero);
            questao.setEnunciado(dto.enunciado());
            questao.setAlternativaCorreta(correta);
            questao.setDisciplina(DisciplinaMapper.paraQuestao(numero));
            questao.setProvaOrigem(prova);
            questao.setSimulado(simulado);

            // Converte alternativas
            List<Alternativa> alternativas = dto.alternativas().stream()
                    .map(altDto -> {
                        Alternativa alt = new Alternativa();
                        alt.setLetra(altDto.letra());
                        alt.setTexto(altDto.texto());
                        alt.setQuestao(questao);
                        return alt;
                    })
                    .collect(Collectors.toList());
            questao.setAlternativas(alternativas);

            questoesValidas.add(questao);
            numerosJaProcessados.add(numero);
        }

        simulado.setQuestoes(questoesValidas);

        // Salva o simulado (cascade salva as questões e alternativas)
        Simulado simuladoSalvo = simuladoRepository.save(simulado);

        // Atualiza status da prova
        prova.setStatus(questoesValidas.isEmpty() ? StatusIngestao.ERRO : StatusIngestao.PROCESSADO);
        provaRepository.save(prova);

        if (questoesValidas.isEmpty()) {
            log.error("Nenhuma questão válida foi extraída para '{}'. Prova salva com status ERRO (ID {}).",
                    edicao, prova.getId());
        }

        log.info("Processamento concluído. Prova: '{}' | Extraídas: {} | Salvas: {} | Ignoradas: {}",
                edicao, questoesExtraidasDto.size(), questoesValidas.size(), descartadas);

        return simuladoSalvo;
    }

    // --- MÉTODO AUXILIAR PARA EXTRAIR O ANO DA EDIÇÃO ---
    private Integer extrairAnoDaEdicao(String edicao) {
        if (edicao == null) {
            return LocalDateTime.now().getYear();
        }

        // Mapeamento das edições conhecidas para seus respectivos anos
        Map<String, Integer> mapaAno = new HashMap<>();
        mapaAno.put("XXXVI", 2022);
        mapaAno.put("XXXVII", 2022);
        mapaAno.put("XX", 2016);
        mapaAno.put("XL", 2023);
        // Adicione outras edições conforme necessário:
        // mapaAno.put("XLI", 2024);
        // mapaAno.put("XLII", 2024);
        // mapaAno.put("XLIII", 2025);

        // Extrai o número romano do início da string
        String romano = edicao.replaceAll(" Exame de Ordem", "").trim();
        // Se a string começar com um número romano válido, usa o mapa
        // Caso contrário, extrai o ano de outra forma (ex: do nome do arquivo)
        Integer ano = mapaAno.get(romano);
        if (ano != null) {
            return ano;
        }

        // Fallback: tenta extrair um ano no formato "YYYY" da string (ex: "2022")
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(edicao);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        // Fallback final: usa o ano atual
        log.warn("Não foi possível extrair o ano da edição '{}'. Usando ano atual.", edicao);
        return LocalDateTime.now().getYear();
    }
}