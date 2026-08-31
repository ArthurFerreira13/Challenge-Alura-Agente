package br.com.agenterag.domain.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "DESEMPENHO_POR_DISCIPLINA")
public class DesempenhoPorDisciplina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", nullable = false)
    private ResultadoSimulado resultado;

    @Enumerated(EnumType.STRING)
    @Column(name = "disciplina", nullable = false, length = 50)
    private Disciplina disciplina;

    @Column(name = "acertos", nullable = false)
    private int acertos;

    @Column(name = "total", nullable = false)
    private int total;

    public Long getId() {
        return id;
    }

    public ResultadoSimulado getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoSimulado resultado) {
        this.resultado = resultado;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
