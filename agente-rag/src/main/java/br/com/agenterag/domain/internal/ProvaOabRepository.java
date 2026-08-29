package br.com.agenterag.domain.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvaOabRepository extends JpaRepository<ProvaOab, Long> {

    Optional<ProvaOab> findByNomeArquivo(String nomeArquivo);
}
