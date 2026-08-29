package br.com.agenterag.domain.dto;

public record ResultadoIngestao(String edicao, boolean sucesso, int questoesSalvas, String mensagem) {

    public static ResultadoIngestao sucesso(String edicao, int questoesSalvas) {
        return new ResultadoIngestao(edicao, true, questoesSalvas,
                "OK — " + questoesSalvas + " questões salvas");
    }

    public static ResultadoIngestao falha(String edicao, String motivo) {
        return new ResultadoIngestao(edicao, false, 0, motivo);
    }
}