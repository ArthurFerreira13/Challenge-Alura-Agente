package br.com.agenterag.domain.internal;

import jakarta.persistence.CascadeType;
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

    private String titulo; // Ex: "Simulado I - OAB 2024"
    private Integer tempoLimiteMinutos; // Ex: 300 (5 horas)

    @OneToMany(mappedBy = "simulado", cascade = CascadeType.ALL)
    private List<Questao> questoes = new ArrayList<>();

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTempoLimiteMinutos(Integer tempoLimiteMinutos) {
        this.tempoLimiteMinutos = tempoLimiteMinutos;
    }

    public void setQuestoes(List<Questao> questoesExtraidas) {
        this.questoes = questoesExtraidas;
    }
}
