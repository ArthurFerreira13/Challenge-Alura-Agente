/*package br.com.agenterag;

import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.service.SimuladoOrchestratorService;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesteDownloadRunner {

    @Bean
    public CommandLineRunner testarIngestao(
            SimuladoOrchestratorService orchestrator,
            PdfDownloadService downloadService) {
        return args -> {
            String urlProva = "https://oab.fgv.br/arq/640/641232_Advogado(EOU)%20Tipo%201.pdf";
            String urlGabarito = "https://oab.fgv.br/arq/640/77817_GABARITOS%20PRELIMINARES_XXXVII_EXAME_DE_ORDEM.pdf";

            System.out.println("=== Baixando prova e gabarito (XXXVII Exame) ===");
            byte[] pdfProva = downloadService.baixarPdfParaBytes(urlProva);
            byte[] pdfGabarito = downloadService.baixarPdfParaBytes(urlGabarito);
            System.out.println("Prova: " + pdfProva.length + " bytes | Gabarito: " + pdfGabarito.length + " bytes");

            try {
                Simulado simulado = orchestrator.ingerirEGerarSimulado(
                        "XXXVII Exame de Ordem", "Advogado_EOU_Tipo_1.pdf", pdfProva, pdfGabarito);

                System.out.println("\n=== RESULTADO ===");
                System.out.println("Simulado ID: " + simulado.getId());
                System.out.println("Total de questões salvas: " + simulado.getQuestoes().size());

                for (Questao q : simulado.getQuestoes()) {
                    System.out.printf("Q%d -> %d alternativas | correta: %s%n",
                            q.getNumeroQuestao(),
                            q.getAlternativas().size(),
                            q.getAlternativaCorreta());
                }

            } catch (Exception e) {
                System.out.println("FALHA na ingestão: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}*/