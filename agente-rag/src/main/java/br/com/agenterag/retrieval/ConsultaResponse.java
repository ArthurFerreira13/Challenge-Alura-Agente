package br.com.agenterag.retrieval;

import java.util.List;

public record ConsultaResponse(String resposta, List<Long> chunkIdsUsados) {
}
