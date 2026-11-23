package br.edu.utfpr.participa.termometro;

import br.edu.utfpr.participa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "resposta_termometro")
public class RespostaTermometro {

    @EmbeddedId
    private RespostaTermometroId id;

    @ManyToOne
    @MapsId("termometroId")
    @JoinColumn(name = "termometro_id")
    private Termometro termometro;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpcaoResposta opcao;

    @Column(name = "respondida_em", nullable = false)
    private Instant respondidaEm;

    protected RespostaTermometro() {
    }

    public RespostaTermometro(Termometro termometro, Usuario usuario, OpcaoResposta opcao) {
        this.id = new RespostaTermometroId(termometro.getId(), usuario.getId());
        this.termometro = termometro;
        this.usuario = usuario;
        this.opcao = opcao;
        this.respondidaEm = Instant.now();
    }

    public RespostaTermometroId getId() {
        return id;
    }

    public Termometro getTermometro() {
        return termometro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public OpcaoResposta getOpcao() {
        return opcao;
    }

    public Instant getRespondidaEm() {
        return respondidaEm;
    }
}