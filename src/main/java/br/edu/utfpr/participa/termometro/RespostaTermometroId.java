package br.edu.utfpr.participa.termometro;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RespostaTermometroId implements Serializable {

    private Long termometroId;
    private Long usuarioId;

    protected RespostaTermometroId() {
    }

    public RespostaTermometroId(Long termometroId, Long usuarioId) {
        this.termometroId = termometroId;
        this.usuarioId = usuarioId;
    }

    public Long getTermometroId() {
        return termometroId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RespostaTermometroId outra)) return false;
        return Objects.equals(termometroId, outra.termometroId) && Objects.equals(usuarioId, outra.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termometroId, usuarioId);
    }
}