package br.edu.utfpr.participa.aula.dto;

import jakarta.validation.constraints.NotBlank;

public record EncerrarAulaRequest(
        @NotBlank(message = "Opção de prazo é obrigatória.") String opcao
) {
}
