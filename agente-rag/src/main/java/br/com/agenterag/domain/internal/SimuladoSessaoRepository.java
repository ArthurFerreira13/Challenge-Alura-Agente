package br.com.agenterag.domain.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SimuladoSessaoRepository extends JpaRepository<SimuladoSessao, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SimuladoSessao s where s.id = :id")
    Optional<SimuladoSessao> findByIdParaAtualizar(@Param("id") Long id);

    void deleteAllByUsuario(Usuario usuario);
}