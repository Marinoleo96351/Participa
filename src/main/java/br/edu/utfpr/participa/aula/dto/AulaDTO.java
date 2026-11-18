package br.edu.utfpr.participa.aula.dto;

import java.time.Instant;
import java.time.LocalDate;

public record AulaDTO(
        Long id,
        Long turmaId,
        Integer numero,
        String titulo,
        LocalDate data,
        String situacao,
        Instant prazoPosAula,
        Instant resumoEnviadoEm
) {
}
