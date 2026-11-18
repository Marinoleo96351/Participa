package br.edu.utfpr.participa.turma.dto;

import jakarta.validation.constraints.NotBlank;

public record EntrarTurmaRequest(
        @NotBlank(message = "O código tem 6 caracteres.") String codigo
) {
}
