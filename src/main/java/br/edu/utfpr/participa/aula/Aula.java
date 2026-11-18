package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.turma.Turma;
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
import java.time.LocalDate;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAula situacao;

    @Column(name = "prazo_pos_aula")
    private Instant prazoPosAula;

    @Column(name = "encerrada_em")
    private Instant encerradaEm;

    protected Aula() {
    }

    public Aula(Turma turma, Integer numero, String titulo, LocalDate data, SituacaoAula situacao) {
        this.turma = turma;
        this.numero = numero;
        this.titulo = titulo;
        this.data = data;
        this.situacao = situacao;
    }

    public void encerrar(Instant prazoPosAula, Instant agora) {
        this.situacao = SituacaoAula.POS_AULA;
        this.prazoPosAula = prazoPosAula;
        this.encerradaEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Turma getTurma() {
        return turma;
    }

    public Integer getNumero() {
        return numero;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDate getData() {
        return data;
    }

    public SituacaoAula getSituacao() {
        return situacao;
    }

    public Instant getPrazoPosAula() {
        return prazoPosAula;
    }

    public Instant getEncerradaEm() {
        return encerradaEm;
    }
}
