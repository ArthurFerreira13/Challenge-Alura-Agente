package br.com.agenterag.domain.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SimuladoSessaoRepository extends JpaRepository<SimuladoSessao, Long> {

    /**
     * Carrega a sessão com lock pessimista de escrita: bloqueia a linha até o
     * fim da transação, evitando que duas chamadas concorrentes a
     * ApuracaoResultadoService.apurar() para a mesma sessão passem pela
     * checagem de status "já concluída" ao mesmo tempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SimuladoSessao s where s.id = :id")
    Optional<SimuladoSessao> findByIdParaAtualizar(@Param("id") Long id);
}
