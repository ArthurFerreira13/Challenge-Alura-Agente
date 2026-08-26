import br.com.agenterag.ingestion.dto.ItemProvaFgv;
import br.com.agenterag.ingestion.service.ScraperService;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class ScraperServiceTest {

    private final ScraperService scraperService = new ScraperService();

    @Test
    void deveExtrairLinksDaSecaoFgv() throws Exception {
        String urlSecao = "https://oab.fgv.br/NovoSec.aspx?key=/OESCjmyOuY=&codSec=5125";

        List<ItemProvaFgv> itens = scraperService.extrairItensDaPagina(urlSecao);

        assertThat(itens).isNotEmpty();

        System.out.println("=== Total de Arquivos Encontrados: " + itens.size() + " ===");
        itens.forEach(item ->
                System.out.printf("[%s] %s -> %s%n",
                        item.isGabarito() ? "GABARITO" : "ARQUIVO/PROVA",
                        item.titulo(),
                        item.urlPdf()
                )
        );
    }
}
