package br.com.agenterag.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "documento" )
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "extensao", nullable = false, length = 10)
    private String extensao;

    @Column(name = "categoria")
    private String categoria;

    @Lob
    @Column(name = "conteudo_original")
    private byte[] conteudoOriginal;

    @Lob
    @Column(name = "texto_extraido")
    private String textoExtraido;

    @Column(name = "data_upload", nullable = false)
    private Instant dataUpload;

    protected Documento() {
    }

    public Documento(String nomeArquivo, String extensao, String categoria,
                     byte[] conteudoOriginal, String textoExtraido) {
        this.nomeArquivo = nomeArquivo;
        this.extensao = extensao;
        this.categoria = categoria;
        this.conteudoOriginal = conteudoOriginal;
        this.textoExtraido = textoExtraido;
        this.dataUpload = Instant.now();
    }

    public Long getId() { return id; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getExtensao() { return extensao; }
    public String getCategoria() { return categoria; }
    public byte[] getConteudoOriginal() { return conteudoOriginal; }
    public String getTextoExtraido() { return textoExtraido; }
    public Instant getDataUpload() { return dataUpload; }
}
