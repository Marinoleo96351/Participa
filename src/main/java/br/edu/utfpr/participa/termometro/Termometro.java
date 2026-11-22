package br.edu.utfpr.participa.termometro;

import br.edu.utfpr.participa.aula.Aula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "termometro")
public class Termometro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @Column(nullable = false)
    private int numero;

    @Column(name = "aberto_em", nullable = false)
    private Instant abertoEm;

    @Column(name = "duracao_segundos", nullable = false)
    private int duracaoSegundos;

    @Column(name = "encerrado_em")
    private Instant encerradoEm;

    protected Termometro() {
    }

    public Termometro(Aula aula, int numero, int duracaoSegundos) {
        this.aula = aula;
        this.numero = numero;
        this.duracaoSegundos = duracaoSegundos;
        this.abertoEm = Instant.now();
    }

    // Aberto = ninguém encerrou na mão E o tempo ainda não acabou.
    public boolean estaAberto(Instant agora) {
        return encerradoEm == null && agora.isBefore(abertoEm.plusSeconds(duracaoSegundos));
    }

    public void encerrar(Instant agora) {
        this.encerradoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Aula getAula() {
        return aula;
    }

    public int getNumero() {
        return numero;
    }

    public Instant getAbertoEm() {
        return abertoEm;
    }

    public int getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public Instant getEncerradoEm() {
        return encerradoEm;
    }
}