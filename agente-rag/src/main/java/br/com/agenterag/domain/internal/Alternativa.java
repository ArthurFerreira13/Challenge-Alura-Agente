package br.com.agenterag.domain.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alternativas")
public class Alternativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1)
    private String letra; // "A", "B", "C", "D"

    @Lob
    @Column(name = "texto")
    private String texto;

    @ManyToOne
    @JoinColumn(name = "questao_id")
    private Questao questao;

    public Long getId() {
        return id;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String s) {
        this.letra = s;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }
}