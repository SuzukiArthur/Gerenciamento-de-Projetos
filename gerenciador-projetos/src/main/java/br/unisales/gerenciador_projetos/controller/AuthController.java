package br.unisales.gerenciador_projetos.controller;

import br.unisales.gerenciador_projetos.dto.LoginRequestDTO;
import br.unisales.gerenciador_projetos.dto.LoginResponseDTO;
import br.unisales.gerenciador_projetos.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Credenciais erradas lançam AuthenticationException, tratada no GlobalExceptionHandler (401)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.login(), dto.senha()));

        String perfil = autenticacao.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(autoridade -> autoridade.replaceFirst("^ROLE_", ""))
                .orElse("USER");

        String token = jwtService.gerarToken(autenticacao.getName(), perfil);

        return ResponseEntity.ok(new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getExpiracaoMs() / 1000,
                autenticacao.getName(),
                perfil));
    }
}
