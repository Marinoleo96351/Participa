package br.edu.utfpr.participa.duvida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuvidaRepository extends JpaRepository<Duvida, Long> {

    List<Duvida> findByAulaId(Long aulaId);
}
