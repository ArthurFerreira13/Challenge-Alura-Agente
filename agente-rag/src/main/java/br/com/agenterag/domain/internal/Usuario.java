package br.com.agenterag.domain.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "senha_hash")
    private String senhaHash; // Preenchido apenas quando provedorAuth == LOCAL

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "google_id", unique = true)
    private String googleId; // Identificador 'sub' retornado pelo Google

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor_auth", nullable = false)
    private ProvedorAuth provedorAuth = ProvedorAuth.GOOGLE;

    @Column(name = "customer_id_mercadopago")
    private String customerIdMercadoPago;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public ProvedorAuth getProvedorAuth() {
        return provedorAuth;
    }

    public void setProvedorAuth(ProvedorAuth provedorAuth) {
        this.provedorAuth = provedorAuth;
    }

    public String getCustomerIdMercadoPago() {
        return customerIdMercadoPago;
    }

    public void setCustomerIdMercadoPago(String customerIdMercadoPago) {
        this.customerIdMercadoPago = customerIdMercadoPago;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}