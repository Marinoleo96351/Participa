package br.edu.utfpr.participa.termometro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AbrirTermometroRequest(
        @Min(value = 15, message = "A duração deve ficar entre 15 e 600 segundos.")
        @Max(value = 600, message = "A duração deve ficar entre 15 e 600 segundos.")
        int duracaoSegundos
) {
}