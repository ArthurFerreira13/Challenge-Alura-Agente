import br.com.agenterag.domain.internal.ProvaOabRepository;
import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.internal.SimuladoRepository;
import br.com.agenterag.domain.service.GabaritoParser;
import br.com.agenterag.domain.service.PdfParserService;
import br.com.agenterag.domain.service.SimuladoOrchestratorService;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SimuladoOrchestratorIntegrationTest {

    @Autowired
    private ProvaOabRepository provaOabRepository;

    @Autowired
    private SimuladoRepository simuladoRepository;

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private GabaritoParser gabaritoParser;

    private final PdfDownloadService downloadService = new PdfDownloadService();

    @Test
    void deveIngerirProvaEGabaritoEGerarSimuladoComRespostasCorretas() throws Exception {
        // URLs reais do XXXIX Exame de Ordem (confirmei o gabarito manualmente;
        // a URL da prova precisa ser confirmada via ScraperServiceTest)
        String urlProva = "<cole aqui a URL real \"Caderno Tipo 1\" que o ScraperServiceTest encontrar>";
        String urlGabarito = "https://oab.fgv.br/arq/642/86039_OABXXXIX%20definitivo%20v20231205.pdf";

        byte[] pdfProva = downloadService.baixarPdfParaBytes(urlProva);
        byte[] pdfGabarito = downloadService.baixarPdfParaBytes(urlGabarito);

        SimuladoOrchestratorService orchestrator = new SimuladoOrchestratorService(
                provaOabRepository, simuladoRepository, pdfParserService, gabaritoParser);

        Simulado simulado = orchestrator.ingerirEGerarSimulado(
                "XXXIX Exame de Ordem", "oab39_prova_tipo1.pdf", pdfProva, pdfGabarito);

        System.out.println("=== RESULTADO ===");
        System.out.println("Simulado ID: " + simulado.getId());
        System.out.println("Total de questões salvas: " + simulado.getQuestoes().size());

        for (Questao q : simulado.getQuestoes()) {
            System.out.printf("Q%d [%s] -> %d alternativas | correta: %s%n",
                    q.getNumeroQuestao(),
                    resumo(q.getEnunciado()),
                    q.getAlternativas().size(),
                    q.getAlternativaCorreta());
        }

        assertThat(simulado.getQuestoes()).isNotEmpty();
        assertThat(simulado.getQuestoes())
                .allSatisfy(q -> assertThat(q.getAlternativaCorreta()).isNotBlank());
    }

    private String resumo(String enunciado) {
        return enunciado.length() > 40 ? enunciado.substring(0, 40) + "..." : enunciado;
    }
}
