package br.com.arthur.agenterag.ingestion.internal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoChunkRepository  extends CrudRepository<DocumentoChunk, Long> {
    List<DocumentoChunk> findByDocumentoId(Long documentoId);
}
