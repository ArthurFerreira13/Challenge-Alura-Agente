package br.com.arthur.agenterag.core.repository;

import br.com.arthur.agenterag.core.domain.Documento;
import org.springframework.data.repository.CrudRepository;

public interface DocumentoRepository extends CrudRepository<Documento, Long> {
}
