package br.com.agenterag.domain.internal;

public enum Disciplina {
    ETICA_ESTATUTO_ADVOCACIA("Ética e Estatuto da Advocacia"),
    DIREITO_CONSTITUCIONAL("Direito Constitucional"),
    DIREITO_ADMINISTRATIVO("Direito Administrativo"),
    DIREITO_CIVIL("Direito Civil"),
    DIREITO_PROCESSUAL_CIVIL("Direito Processual Civil"),
    DIREITO_PENAL("Direito Penal"),
    DIREITO_PROCESSUAL_PENAL("Direito Processual Penal"),
    DIREITO_DO_TRABALHO("Direito do Trabalho"),
    DIREITO_PROCESSUAL_DO_TRABALHO("Direito Processual do Trabalho"),
    DIREITO_EMPRESARIAL("Direito Empresarial"),
    DIREITO_TRIBUTARIO("Direito Tributário"),
    DIREITO_INTERNACIONAL("Direito Internacional"),
    DIREITOS_HUMANOS("Direitos Humanos"),
    DIREITO_AMBIENTAL("Direito Ambiental"),
    DIREITO_ELEITORAL("Direito Eleitoral"),
    ECA("Estatuto da Criança e do Adolescente"),
    DIREITO_DO_CONSUMIDOR("Direito do Consumidor"),
    FILOSOFIA_DO_DIREITO("Filosofia do Direito"),
    DIREITO_FINANCEIRO("Direito Financeiro"),
    DESCONHECIDA("Não identificada");

    private final String descricao;

    Disciplina(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Tenta casar um texto livre extraído do PDF com uma disciplina conhecida. */
    public static Disciplina identificar(String textoLivre) {
        if (textoLivre == null || textoLivre.isBlank()) {
            return DESCONHECIDA;
        }
        String normalizado = textoLivre.toUpperCase().trim();
        for (Disciplina d : values()) {
            String descNormalizada = normalizeAcentos(d.descricao.toUpperCase());
            if (normalizeAcentos(normalizado).contains(descNormalizada)) {
                return d;
            }
        }
        return DESCONHECIDA;
    }

    private static String normalizeAcentos(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}