package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "apoio")
public class Apoio {

    @EmbeddedId
    private ApoioId id;

    @ManyToOne
    @MapsId("duvidaId")
    @JoinColumn(name = "duvida_id")
    private Duvida duvida;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Apoio() {
    }

    public Apoio(Duvida duvida, Usuario usuario) {
        this.id = new ApoioId(duvida.getId(), usuario.getId());
        this.duvida = duvida;
        this.usuario = usuario;
        this.criadoEm = Instant.now();
    }

    public ApoioId getId() {
        return id;
    }

    public Duvida getDuvida() {
        return duvida;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
