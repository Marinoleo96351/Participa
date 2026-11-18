package br.edu.utfpr.participa.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroRequest(
        @NotBlank(message = "Nome é obrigatório.") String nome,
        @NotBlank(message = "E-mail é obrigatório.") String email,
        @NotBlank(message = "Senha é obrigatória.") String senha,
        @NotBlank(message = "Perfil é obrigatório.") String perfil
) {
}
