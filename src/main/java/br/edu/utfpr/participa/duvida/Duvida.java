package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "duvida")
public class Duvida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(nullable = false)
    private boolean anonima;

    @Column(nullable = false, length = 500)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDuvida status;

    @Column(name = "termo_retido", length = 80)
    private String termoRetido;

    @Column(name = "pos_aula", nullable = false)
    private boolean posAula;

    @Column(nullable = false)
    private boolean respondida;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected Duvida() {
    }

    public Duvida(Aula aula, Usuario autor, boolean anonima, String texto, StatusDuvida status, boolean posAula) {
        this.aula = aula;
        this.autor = autor;
        this.anonima = anonima;
        this.texto = texto;
        this.status = status;
        this.posAula = posAula;
        this.respondida = false;
        this.criadaEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Aula getAula() {
        return aula;
    }

    public Usuario getAutor() {
        return autor;
    }

    public boolean isAnonima() {
        return anonima;
    }

    public String getTexto() {
        return texto;
    }

    public StatusDuvida getStatus() {
        return status;
    }

    public String getTermoRetido() {
        return termoRetido;
    }

    public boolean isPosAula() {
        return posAula;
    }

    public boolean isRespondida() {
        return respondida;
    }

    public void marcarRespondida(boolean respondida) {
        this.respondida = respondida;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
