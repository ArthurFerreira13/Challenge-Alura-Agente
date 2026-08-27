package br.com.agenterag.ingestion.dto;

public record ItemProvaFgv(
        String titulo,
        String urlPdf,
        boolean isGabarito
) {
    public boolean ehGabarito() {
        return isGabarito;
    }
}
