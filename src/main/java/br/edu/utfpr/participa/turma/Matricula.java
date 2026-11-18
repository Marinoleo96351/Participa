package br.edu.utfpr.participa.turma;

import br.edu.utfpr.participa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "matricula")
public class Matricula {

    @EmbeddedId
    private MatriculaId id;

    @ManyToOne
    @MapsId("turmaId")
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected Matricula() {
    }

    public Matricula(Turma turma, Usuario usuario) {
        this.id = new MatriculaId(turma.getId(), usuario.getId());
        this.turma = turma;
        this.usuario = usuario;
        this.criadaEm = Instant.now();
    }

    public MatriculaId getId() {
        return id;
    }

    public Turma getTurma() {
        return turma;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
