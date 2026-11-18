package br.edu.utfpr.participa.turma;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    boolean existsByCodigoAcesso(String codigoAcesso);

    Optional<Turma> findByCodigoAcesso(String codigoAcesso);

    List<Turma> findByProfessorIdOrderByNomeAsc(Long professorId);

    Optional<Turma> findByIdAndProfessorId(Long id, Long professorId);
}
