package br.com.arthur.agenterag.ingestion;

public record ExtracaoResponse(
        String nomeArquivo,
        String extensao,
        int totalCaracteres,
        String textoExtraido
) {
    public static ExtracaoResponse de(String nomeArquivo, String extensao, String texto) {
        return new ExtracaoResponse(nomeArquivo, extensao, texto.length(), texto);
    }

}
