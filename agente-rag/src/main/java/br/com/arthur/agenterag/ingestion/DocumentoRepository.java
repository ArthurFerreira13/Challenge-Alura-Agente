package br.com.arthur.agenterag.ingestion;

import br.com.arthur.agenterag.ingestion.internal.Documento;
import org.springframework.data.repository.CrudRepository;

public interface DocumentoRepository extends CrudRepository<Documento, Long> {
}
