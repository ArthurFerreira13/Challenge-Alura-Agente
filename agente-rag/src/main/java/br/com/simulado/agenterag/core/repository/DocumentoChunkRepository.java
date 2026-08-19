package br.com.simulado.agenterag.core.repository;

import br.com.simulado.agenterag.core.domain.DocumentoChunk;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoChunkRepository  extends CrudRepository<DocumentoChunk, Long> {
    List<DocumentoChunk> findByDocumentoId(Long documentoId);
}
