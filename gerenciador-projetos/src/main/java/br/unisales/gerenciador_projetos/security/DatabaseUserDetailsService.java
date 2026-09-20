package br.unisales.gerenciador_projetos.security;

import br.unisales.gerenciador_projetos.entity.Usuario;
import br.unisales.gerenciador_projetos.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

// Busca o usuário no banco pelo login e transforma funcao em uma autoridade //

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public DatabaseUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String funcao = usuario.getFuncao() == null || usuario.getFuncao().isBlank()
                ? "USER"
                : usuario.getFuncao().trim().toUpperCase(Locale.ROOT);

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenha())
                .authorities(new SimpleGrantedAuthority("ROLE_" + funcao))
                .build();
    }
}