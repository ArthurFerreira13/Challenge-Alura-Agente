import br.com.agenterag.ingestion.service.PdfDownloadService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PdfDownloadServiceTest {
    private final PdfDownloadService pdfDownloadService = new PdfDownloadService();

    @Test
    void deveBaixarArquivoPdfComSucesso() throws Exception {
        // URL obtida do seu scrape anterior
        String urlPdf = "https://oab.fgv.br/arq/644/84844_oab242_gabarito_definitivo.pdf";

        Path arquivoBaixado = pdfDownloadService.baixarPdfParaArquivo(urlPdf);

        assertThat(Files.exists(arquivoBaixado)).isTrue();
        assertThat(Files.size(arquivoBaixado)).isGreaterThan(0);

        System.out.println("Arquivo baixado temporariamente em: " + arquivoBaixado.toAbsolutePath());

        // Limpeza do teste
        Files.deleteIfExists(arquivoBaixado);
    }
}
