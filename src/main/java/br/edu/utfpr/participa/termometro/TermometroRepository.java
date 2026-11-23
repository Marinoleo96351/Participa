package br.edu.utfpr.participa.termometro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermometroRepository extends JpaRepository<Termometro, Long> {

    // Termômetros da aula que ninguém encerrou na mão (o prazo é checado na entidade).
    List<Termometro> findByAulaIdAndEncerradoEmIsNull(Long aulaId);

    // Mais recente da aula: é o "termômetro ativo" ou o último resultado.
    Optional<Termometro> findFirstByAulaIdOrderByNumeroDesc(Long aulaId);

    List<Termometro> findByAulaIdOrderByNumeroDesc(Long aulaId);

    Optional<Termometro> findByIdAndAula_Turma_Professor_Id(Long id, Long professorId);

    @Query("select coalesce(max(t.numero), 0) from Termometro t where t.aula.id = :aulaId")
    int buscarMaiorNumero(@Param("aulaId") Long aulaId);
}