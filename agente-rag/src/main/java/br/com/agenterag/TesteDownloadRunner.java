package br.com.agenterag;


import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.internal.Simulado;
import br.com.agenterag.domain.service.SimuladoOrchestratorService;
import br.com.agenterag.ingestion.dto.ItemProvaFgv;
import br.com.agenterag.ingestion.service.PdfDownloadService;
import br.com.agenterag.ingestion.service.ScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Configuration
public class TesteDownloadRunner {
    @Bean
    public CommandLineRunner testarIngestao(SimuladoOrchestratorService orchestrator) {
        return args -> {
            Path pastaDownloads = Paths.get("downloads-teste");

            Path arquivoProva = pastaDownloads.resolve("Caderno_de_Prova___Tipo_1.pdf");
            Path arquivoGabarito = pastaDownloads.resolve("Gabaritos_definitivos_da_prova_objetiva__1__fase_.pdf");

            if (!Files.exists(arquivoProva) || !Files.exists(arquivoGabarito)) {
                System.out.println("ERRO: arquivos não encontrados em " + pastaDownloads.toAbsolutePath());
                System.out.println("Prova existe? " + Files.exists(arquivoProva));
                System.out.println("Gabarito existe? " + Files.exists(arquivoGabarito));
                return;
            }

            byte[] pdfProva = Files.readAllBytes(arquivoProva);
            byte[] pdfGabarito = Files.readAllBytes(arquivoGabarito);

            System.out.println("=== Iniciando ingestão no banco ===");
            System.out.println("Prova: " + pdfProva.length + " bytes | Gabarito: " + pdfGabarito.length + " bytes");

            try {
                Simulado simulado = orchestrator.ingerirEGerarSimulado(
                        "45o Exame de Ordem", "Caderno_Tipo_1.pdf", pdfProva, pdfGabarito);

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
}
