package br.unisales.gerenciador_projetos.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "O login é obrigatório")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {

    public LoginRequestDTO {
        // Mesmo tratamento do cadastro (UsuarioRequestDTO), que também remove espaços das pontas
        if (login != null) {
            login = login.trim();
            if (login.isBlank()) {
                login = null;
            }
        }

        if (senha != null) {
            senha = senha.trim();
            if (senha.isBlank()) {
                senha = null;
            }
        }
    }
}
