package br.edu.utfpr.participa.turma;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MatriculaId implements Serializable {

    private Long turmaId;
    private Long usuarioId;

    protected MatriculaId() {
    }

    public MatriculaId(Long turmaId, Long usuarioId) {
        this.turmaId = turmaId;
        this.usuarioId = usuarioId;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatriculaId outra)) return false;
        return Objects.equals(turmaId, outra.turmaId) && Objects.equals(usuarioId, outra.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turmaId, usuarioId);
    }
}
