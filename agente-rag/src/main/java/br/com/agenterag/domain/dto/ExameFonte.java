package br.com.agenterag.domain.dto;

public record ExameFonte(String edicao, String urlSecaoFgv, String urlProvaDireta, String urlGabaritoDireto) {

    /** Descobre a prova/gabarito automaticamente via scraping da página de seção. */
    public static ExameFonte porSecao(String edicao, String urlSecaoFgv) {
        return new ExameFonte(edicao, urlSecaoFgv, null, null);
    }

    /** Usa URLs de prova e gabarito já conhecidas, sem depender do scraper. */
    public static ExameFonte porUrlsDiretas(String edicao, String urlProva, String urlGabarito) {
        return new ExameFonte(edicao, null, urlProva, urlGabarito);
    }

    public boolean temUrlsDiretas() {
        return urlProvaDireta != null && urlGabaritoDireto != null;
    }
}