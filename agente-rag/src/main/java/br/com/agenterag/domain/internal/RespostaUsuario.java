package br.com.agenterag.domain.internal;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resposta_usuario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sessao_id", "questao_id"}))
public class RespostaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SimuladoSessao sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(name = "alternativa_escolhida", length = 1)
    private String alternativaEscolhida;

    @Column(name = "respondido_em", nullable = false)
    private LocalDateTime respondidoEm;

    public Long getId() {
        return id;
    }

    public SimuladoSessao getSessao() {
        return sessao;
    }

    public void setSessao(SimuladoSessao sessao) {
        this.sessao = sessao;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public String getAlternativaEscolhida() {
        return alternativaEscolhida;
    }

    public void setAlternativaEscolhida(String alternativaEscolhida) {
        this.alternativaEscolhida = alternativaEscolhida;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }
}
