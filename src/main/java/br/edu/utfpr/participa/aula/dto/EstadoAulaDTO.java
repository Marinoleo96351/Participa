package br.edu.utfpr.participa.aula.dto;

import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;

import java.time.Instant;
import java.util.List;

public record EstadoAulaDTO(
        AulaDTO aula,
        List<DuvidaDTO> duvidas,
        Object termometro,
        Instant servidorAgora
) {
}
