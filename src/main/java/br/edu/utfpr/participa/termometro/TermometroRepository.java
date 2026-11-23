package br.edu.utfpr.participa.termometro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermometroRepository extends JpaRepository<Termometro, Long> {

    List<Termometro> findByAula_IdOrderByNumeroAsc(Long aulaId);

    Optional<Termometro> findFirstByAula_IdOrderByNumeroDesc(Long aulaId);
}