package br.edu.utfpr.participa.turma.dto;

import java.time.LocalDate;

public record TurmaDTO(
        Long id,
        String nome,
        String codigo,
        LocalDate fimSemestre,
        Integer estudantes
) {
}
