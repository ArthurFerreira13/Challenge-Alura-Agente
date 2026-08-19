package br.com.simulado.agenterag.core.repository;

import br.com.simulado.agenterag.core.domain.Documento;
import org.springframework.data.repository.CrudRepository;

public interface DocumentoRepository extends CrudRepository<Documento, Long> {
}
