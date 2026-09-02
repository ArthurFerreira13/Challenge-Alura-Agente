package br.com.agenterag.domain.internal;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulado_sessao")
public class SimuladoSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulado_id", nullable = false)
    private Simulado simulado;

    @Column(name = "indice_atual", nullable = false)
    private int indiceAtual = 0;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSessao status = StatusSessao.EM_ANDAMENTO;

    @OneToMany(mappedBy = "sessao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaUsuario> respostas = new ArrayList<>();

    // --- MÉTODOS DE REGRAS DE TEMPO / CONTAGEM ---

    public LocalDateTime getDataHoraExpiracao() {
        if (iniciadoEm == null || simulado == null) {
            return null;
        }
        return iniciadoEm.plusMinutes(simulado.getTempoLimiteMinutos());
    }

    public long getTempoRestanteSegundos(LocalDateTime agora) {
        if (status != StatusSessao.EM_ANDAMENTO || iniciadoEm == null || simulado == null) {
            return 0;
        }
        LocalDateTime expiracao = getDataHoraExpiracao();
        long segundosRestantes = Duration.between(agora, expiracao).getSeconds();
        return Math.max(0, segundosRestantes);
    }

    public boolean isExpirada(LocalDateTime agora) {
        if (status != StatusSessao.EM_ANDAMENTO || iniciadoEm == null || simulado == null) {
            return false;
        }
        return agora.isAfter(getDataHoraExpiracao());
    }

    // --- GETTERS E SETTERS ---

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Simulado getSimulado() {
        return simulado;
    }

    public void setSimulado(Simulado simulado) {
        this.simulado = simulado;
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }

    public void setIndiceAtual(int indiceAtual) {
        this.indiceAtual = indiceAtual;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public void setIniciadoEm(LocalDateTime iniciadoEm) {
        this.iniciadoEm = iniciadoEm;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public void setConcluidoEm(LocalDateTime concluidoEm) {
        this.concluidoEm = concluidoEm;
    }

    public StatusSessao getStatus() {
        return status;
    }

    public void setStatus(StatusSessao status) {
        this.status = status;
    }

    public List<RespostaUsuario> getRespostas() {
        return respostas;
    }

    public Long getSimuladoId() {
        return simulado != null ? simulado.getId() : null;
    }

    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }
}