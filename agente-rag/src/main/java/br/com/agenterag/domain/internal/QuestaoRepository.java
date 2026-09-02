package br.com.agenterag.domain.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    Page<Questao> findByDisciplina(Disciplina disciplina, Pageable pageable);
}