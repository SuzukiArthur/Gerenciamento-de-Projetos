package br.unisales.gerenciador_projetos.dto;

public record LoginResponseDTO(
        String token,
        String tipo,
        long expiraEmSegundos,
        String login,
        String perfil
) {
}
