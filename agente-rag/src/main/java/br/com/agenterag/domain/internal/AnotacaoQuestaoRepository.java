package br.com.agenterag.domain.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnotacaoQuestaoRepository extends JpaRepository<AnotacaoQuestao, Long> {
    Optional<AnotacaoQuestao> findByUsuarioIdAndQuestaoId(Long usuarioId, Long questaoId);
    void deleteAllByUsuarioId(Long usuarioId);
}