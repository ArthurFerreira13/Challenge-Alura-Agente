package br.com.agenterag.domain.internal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulados")
public class Simulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "tempo_limite_minutos", nullable = false)
    private Integer tempoLimiteMinutos;

    @OneToMany(mappedBy = "simulado", cascade = CascadeType.ALL)
    private List<Questao> questoes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTempoLimiteMinutos() {
        return tempoLimiteMinutos;
    }

    public void setTempoLimiteMinutos(Integer tempoLimiteMinutos) {
        this.tempoLimiteMinutos = tempoLimiteMinutos;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
}