package br.edu.utfpr.participa.termometro.dto;

import br.edu.utfpr.participa.termometro.OpcaoResposta;
import jakarta.validation.constraints.NotNull;

public record ResponderTermometroRequest(
        @NotNull(message = "Escolha uma das opções.") OpcaoResposta opcao
) {
}