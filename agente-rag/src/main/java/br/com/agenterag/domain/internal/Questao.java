package br.com.agenterag.domain.internal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questoes")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numeroQuestao; // Ex: 1

    @Lob
    @Column(name = "enunciado")
    private String enunciado;

    @Column(length = 100)
    private String disciplina; // Ex: "Direito Constitucional", "Direito Penal"

    @ManyToOne
    @JoinColumn(name = "prova_origem_id")
    private ProvaOab provaOrigem;

    @ManyToOne
    @JoinColumn(name = "simulado_id")
    private Simulado simulado;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alternativa> alternativas = new ArrayList<>();

    @Column(name = "alternativa_correta", length = 1)
    private String alternativaCorreta; // Ex: "A", "B", "C", "D"

    public Long getId() {
        return id;
    }

    public Integer getNumeroQuestao() {
        return numeroQuestao;
    }

    public void setNumeroQuestao(int numero) {
        this.numeroQuestao = numero;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public ProvaOab getProvaOrigem() {
        return provaOrigem;
    }

    public void setProvaOrigem(ProvaOab prova) {
        this.provaOrigem = prova;
    }

    public Simulado getSimulado() {
        return simulado;
    }

    public void setSimulado(Simulado simulado) {
        this.simulado = simulado;
    }

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
        if (alternativas != null) {
            alternativas.forEach(alt -> alt.setQuestao(this));
        }
    }

    public String getAlternativaCorreta() {
        return alternativaCorreta;
    }

    public void setAlternativaCorreta(String alternativaCorreta) {
        this.alternativaCorreta = alternativaCorreta;
    }
}
