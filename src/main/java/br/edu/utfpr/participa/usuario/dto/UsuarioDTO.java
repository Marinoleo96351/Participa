package br.edu.utfpr.participa.usuario.dto;

public record UsuarioDTO(
        Long id,
        String nome,
        String email,
        String perfil,
        boolean aceiteTermo,
        boolean orientacaoVista
) {
}
