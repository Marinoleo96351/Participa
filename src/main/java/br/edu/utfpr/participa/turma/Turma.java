package br.edu.utfpr.participa.turma;

import br.edu.utfpr.participa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "codigo_acesso", nullable = false, unique = true, length = 6)
    private String codigoAcesso;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @Column(name = "fim_semestre", nullable = false)
    private LocalDate fimSemestre;

    protected Turma() {
    }

    public Turma(String nome, String codigoAcesso, Usuario professor, LocalDate fimSemestre) {
        this.nome = nome;
        this.codigoAcesso = codigoAcesso;
        this.professor = professor;
        this.fimSemestre = fimSemestre;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigoAcesso() {
        return codigoAcesso;
    }

    public Usuario getProfessor() {
        return professor;
    }

    public LocalDate getFimSemestre() {
        return fimSemestre;
    }
}
