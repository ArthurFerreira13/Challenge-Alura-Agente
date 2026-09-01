package br.com.agenterag.domain.dto;

import br.com.agenterag.domain.internal.Alternativa;

// Sem alternativaCorreta de propósito: essa resposta vai pro cliente
// ANTES da apuração, não pode vazar o gabarito.
public record AlternativaResponse(String letra, String texto) {
    public static AlternativaResponse from(Alternativa alternativa) {
        return new AlternativaResponse(alternativa.getLetra(), alternativa.getTexto());
    }
}
