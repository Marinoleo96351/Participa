package br.edu.utfpr.participa.aula.dto;

import jakarta.validation.constraints.NotBlank;

public record AbrirAulaRequest(
        @NotBlank(message = "Título é obrigatório.") String titulo
) {
}
