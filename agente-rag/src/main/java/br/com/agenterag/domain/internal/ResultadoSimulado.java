package br.com.agenterag.domain.internal;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resultado_simulado")
public class ResultadoSimulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false, unique = true)
    private SimuladoSessao sessao;

    @Column(name = "total_acertos", nullable = false)
    private int totalAcertos;

    @Column(name = "total_erros", nullable = false)
    private int totalErros;

    @Column(name = "total_em_branco", nullable = false)
    private int totalEmBranco;

    @Column(name = "percentual_acerto", nullable = false)
    private double percentualAcerto;

    @Column(name = "aprovado", nullable = false)
    private boolean aprovado;

    @Column(name = "tempo_gasto_segundos", nullable = false)
    private long tempoGastoSegundos;

    @OneToMany(mappedBy = "resultado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesempenhoPorDisciplina> desempenhoPorDisciplina = new ArrayList<>();

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    public Long getId() {
        return id;
    }

    public SimuladoSessao getSessao() {
        return sessao;
    }

    public void setSessao(SimuladoSessao sessao) {
        this.sessao = sessao;
    }

    public int getTotalAcertos() {
        return totalAcertos;
    }

    public void setTotalAcertos(int totalAcertos) {
        this.totalAcertos = totalAcertos;
    }

    public int getTotalErros() {
        return totalErros;
    }

    public void setTotalErros(int totalErros) {
        this.totalErros = totalErros;
    }

    public int getTotalEmBranco() {
        return totalEmBranco;
    }

    public void setTotalEmBranco(int totalEmBranco) {
        this.totalEmBranco = totalEmBranco;
    }

    public double getPercentualAcerto() {
        return percentualAcerto;
    }

    public void setPercentualAcerto(double percentualAcerto) {
        this.percentualAcerto = percentualAcerto;
    }

    public boolean isAprovado() {
        return aprovado;
    }

    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }

    public long getTempoGastoSegundos() {
        return tempoGastoSegundos;
    }

    public void setTempoGastoSegundos(long tempoGastoSegundos) {
        this.tempoGastoSegundos = tempoGastoSegundos;
    }

    public List<DesempenhoPorDisciplina> getDesempenhoPorDisciplina() {
        return desempenhoPorDisciplina;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(LocalDateTime geradoEm) {
        this.geradoEm = geradoEm;
    }
}
