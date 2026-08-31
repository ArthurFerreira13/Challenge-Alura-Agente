package br.com.agenterag.domain.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespostaUsuarioRepository extends JpaRepository<RespostaUsuario, Long> {

    List<RespostaUsuario> findBySessaoId(Long sessaoId);

    Optional<RespostaUsuario> findBySessaoIdAndQuestaoId(Long sessaoId, Long questaoId);
}