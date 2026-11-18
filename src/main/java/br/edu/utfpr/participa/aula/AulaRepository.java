package br.edu.utfpr.participa.aula;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByTurmaIdOrderByNumeroDesc(Long turmaId);

    boolean existsByTurmaIdAndSituacao(Long turmaId, SituacaoAula situacao);

    Optional<Aula> findByIdAndTurma_Professor_Id(Long id, Long professorId);

    @Query("select max(a.numero) from Aula a where a.turma.id = :turmaId")
    Optional<Integer> buscarMaiorNumero(@Param("turmaId") Long turmaId);

    @Query("""
            select a from Aula a
            where a.turma.id in :turmaIds and a.situacao in :situacoes
            order by a.data desc, a.numero desc
            """)
    List<Aula> buscarAtivasDasTurmas(@Param("turmaIds") Collection<Long> turmaIds,
                                      @Param("situacoes") Collection<SituacaoAula> situacoes);
}
