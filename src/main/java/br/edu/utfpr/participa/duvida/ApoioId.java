package br.edu.utfpr.participa.duvida;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ApoioId implements Serializable {

    private Long duvidaId;
    private Long usuarioId;

    protected ApoioId() {
    }

    public ApoioId(Long duvidaId, Long usuarioId) {
        this.duvidaId = duvidaId;
        this.usuarioId = usuarioId;
    }

    public Long getDuvidaId() {
        return duvidaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApoioId outra)) return false;
        return Objects.equals(duvidaId, outra.duvidaId) && Objects.equals(usuarioId, outra.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duvidaId, usuarioId);
    }
}
