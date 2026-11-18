package br.edu.utfpr.participa.turma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CriarTurmaRequest(
        @NotBlank(message = "Nome da turma é obrigatório.") String nome,
        @NotNull(message = "Fim do semestre é obrigatório.") LocalDate fimSemestre
) {
}
