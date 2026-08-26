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

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL)
    private List<Alternativa> alternativas = new ArrayList<>();

    @Column(name = "alternativa_correta", length = 1)
    private String alternativaCorreta; // Ex: "A", "B", "C", "D"

    public void setProvaOrigem(ProvaOab prova) {
        this.provaOrigem = prova;
    }

    public void setSimulado(Simulado simulado) {
        this.simulado = simulado;
    }

}
