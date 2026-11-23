package br.edu.utfpr.participa.termometro.dto;

public record ResultadoTermometroDTO(
        long entendi,
        long parcial,
        long naoEntendi,
        long respostas,
        long matriculados) {
}