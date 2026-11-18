package br.edu.utfpr.participa.turma;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, MatriculaId> {

    boolean existsById_TurmaIdAndId_UsuarioId(Long turmaId, Long usuarioId);

    long countById_TurmaId(Long turmaId);

    List<Matricula> findById_UsuarioIdOrderByTurma_NomeAsc(Long usuarioId);
}
