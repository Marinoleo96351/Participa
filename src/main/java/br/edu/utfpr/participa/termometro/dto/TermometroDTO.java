package br.edu.utfpr.participa.termometro.dto;

import java.time.Instant;

import br.edu.utfpr.participa.termometro.OpcaoResposta;

public record TermometroDTO(
        Long id,
        int numero,
        Instant abertoEm,
        int duracaoSegundos,
        boolean aberto,
        OpcaoResposta minhaResposta,
        ResultadoTermometroDTO resultado) {
}