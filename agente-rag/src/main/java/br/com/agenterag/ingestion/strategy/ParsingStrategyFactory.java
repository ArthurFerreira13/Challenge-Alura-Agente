package br.com.agenterag.ingestion.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParsingStrategyFactory {

    private final List<ParsingStrategy> strategies;

    public ParsingStrategyFactory(List<ParsingStrategy> strategies) {
        this.strategies = strategies;
    }

    public ParsingStrategy getStrategy(String nomeArquivo, String urlProva) {
        return strategies.stream()
                .filter(s -> s.supports(nomeArquivo, urlProva))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhuma estratégia de parsing encontrada para: " + nomeArquivo + " (URL: " + urlProva + ")"
                ));
    }
}