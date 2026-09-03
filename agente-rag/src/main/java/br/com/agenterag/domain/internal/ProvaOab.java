package br.com.agenterag.domain.internal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prova_oab")
public class ProvaOab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String edicao; // Ex: "XXXVIII Exame de Ordem"
    private String nomeArquivo;

    @Column(name = "ano")  // <-- ADICIONADO
    private Integer ano;   // <-- ADICIONADO

    @Lob
    private byte[] conteudoPdf;

    @Enumerated(EnumType.STRING)
    private StatusIngestao status; // PENDENTE, PROCESSADO, ERRO

    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "provaOrigem", cascade = CascadeType.ALL)
    private List<Questao> questoes = new ArrayList<>();

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public String getEdicao() {
        return edicao;
    }

    public void setEdicao(String edicao) {
        this.edicao = edicao;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public Integer getAno() {              // <-- ADICIONADO
        return ano;
    }

    public void setAno(Integer ano) {      // <-- ADICIONADO
        this.ano = ano;
    }

    public byte[] getConteudoPdf() {
        return conteudoPdf;
    }

    public void setConteudoPdf(byte[] conteudoPdf) {
        this.conteudoPdf = conteudoPdf;
    }

    public StatusIngestao getStatus() {
        return status;
    }

    public void setStatus(StatusIngestao statusIngestao) {
        this.status = statusIngestao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }
}