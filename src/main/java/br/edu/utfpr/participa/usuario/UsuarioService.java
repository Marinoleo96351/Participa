package br.edu.utfpr.participa.usuario;

import br.edu.utfpr.participa.usuario.dto.CadastroRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String dominioEstudante;
    private final String dominioProfessor;

    public UsuarioService(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.dominio-estudante}") String dominioEstudante,
                           @Value("${app.dominio-professor}") String dominioProfessor) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.dominioEstudante = dominioEstudante;
        this.dominioProfessor = dominioProfessor;
    }

    public Usuario cadastrar(CadastroRequest requisicao) {
        Perfil perfil = interpretarPerfil(requisicao.perfil());
        validarDominio(perfil, requisicao.email());
        if (requisicao.senha().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha precisa ter pelo menos 8 caracteres.");
        }
        if (usuarioRepository.existsByEmail(requisicao.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        Usuario usuario = new Usuario(
                requisicao.nome(),
                requisicao.email(),
                passwordEncoder.encode(requisicao.senha()),
                perfil);
        return usuarioRepository.save(usuario);
    }

    private Perfil interpretarPerfil(String valor) {
        Perfil perfil;
        try {
            perfil = Perfil.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil precisa ser ESTUDANTE ou PROFESSOR.");
        }
        if (perfil != Perfil.ESTUDANTE && perfil != Perfil.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil precisa ser ESTUDANTE ou PROFESSOR.");
        }
        return perfil;
    }

    private void validarDominio(Perfil perfil, String email) {
        String dominioEsperado = perfil == Perfil.ESTUDANTE ? dominioEstudante : dominioProfessor;
        if (!email.toLowerCase().endsWith(dominioEsperado.toLowerCase())) {
            String nomePerfil = perfil == Perfil.ESTUDANTE ? "estudante" : "professor";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "E-mail de %s precisa ser do domínio %s.".formatted(nomePerfil, dominioEsperado));
        }
    }
}
