package br.com.simulado.agenterag.retrieval.dto;

import java.util.List;

public record ConsultaResponse(String resposta, List<Long> chunkIdsUsados) {
}
