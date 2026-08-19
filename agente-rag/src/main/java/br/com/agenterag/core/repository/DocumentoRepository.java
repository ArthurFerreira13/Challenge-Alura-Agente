package br.com.agenterag.core.repository;

import br.com.agenterag.core.domain.Documento;
import org.springframework.data.repository.CrudRepository;

public interface DocumentoRepository extends CrudRepository<Documento, Long> {
}
