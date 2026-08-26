package br.com.agenterag.ingestion.service;

import br.com.agenterag.ingestion.dto.ItemProvaFgv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    public List<ItemProvaFgv> extrairItensDaPagina(String url) throws IOException {
        List<ItemProvaFgv> itens = new ArrayList<>();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://oab.fgv.br/")
                .timeout(10000)
                .get();

        Elements links = doc.select("a[href*=.pdf], a[href*=.zip]");

        for (Element link : links) {
            String titulo = link.text().trim();
            String href = link.absUrl("href");

            if (!href.isEmpty()) {
                // Força HTTPS para evitar redirecionamentos em chamadas futuras
                if (href.startsWith("http://")) {
                    href = href.replaceFirst("http://", "https://");
                }

                String tituloLower = titulo.toLowerCase();
                boolean ehGabarito = tituloLower.contains("gabarito")
                        || tituloLower.contains("padrão de respostas")
                        || tituloLower.contains("padrao de respostas");

                itens.add(new ItemProvaFgv(titulo, href, ehGabarito));
            }
        }

        return itens;
    }
}
