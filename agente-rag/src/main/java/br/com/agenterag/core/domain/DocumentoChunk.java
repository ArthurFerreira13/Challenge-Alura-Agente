package br.com.agenterag.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento_chunk")
public class DocumentoChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documento_id", nullable = false)
    private Long documentoId;

    @Lob
    @Column(name = "conteudo_chunk", nullable = false)
    private String conteudoChunk;

    @Lob
    @Column(name = "vetor_embedding")
    private String vetorEmbedding;

    protected DocumentoChunk() {}

    public DocumentoChunk(Long documentoId, String conteudoChunk, String vetorEmbedding) {
        this.documentoId = documentoId;
        this.conteudoChunk = conteudoChunk;
        this.vetorEmbedding = vetorEmbedding;
    }

    public Long getId() { return id; }
    public Long getDocumentoId() { return documentoId; }
    public String getConteudoChunk() { return conteudoChunk; }
    public String getVetorEmbedding() { return vetorEmbedding; }

    // Setter necessário para atualizar o vetor após a chamada da OpenAI
    public void setVetorEmbedding(String vetorEmbedding) {
        this.vetorEmbedding = vetorEmbedding;
    }
}