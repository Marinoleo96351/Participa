package br.edu.utfpr.participa.duvida.dto;

import java.time.Instant;

public record DuvidaDTO(
        Long id,
        String texto,
        boolean anonima,
        String autorNome,
        boolean minha,
        int apoios,
        boolean apoiadaPorMim,
        boolean respondida,
        Long temaId,
        boolean posAula,
        String status,
        String termoRetido,
        Instant criadaEm
) {
}
