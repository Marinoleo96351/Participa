package br.edu.utfpr.participa.usuario;

import br.edu.utfpr.participa.usuario.dto.CadastroRequest;
import br.edu.utfpr.participa.usuario.dto.LoginRequest;
import br.edu.utfpr.participa.usuario.dto.UsuarioDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UsuarioService usuarioService, UsuarioMapper usuarioMapper,
                           AuthenticationManager authenticationManager,
                           SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/cadastro")
    ResponseEntity<UsuarioDTO> cadastro(@Valid @RequestBody CadastroRequest requisicao) {
        Usuario usuario = usuarioService.cadastrar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toDTO(usuario));
    }

    @PostMapping("/login")
    UsuarioDTO login(@Valid @RequestBody LoginRequest requisicao,
                      HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requisicao.email(), requisicao.senha()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return usuarioMapper.toDTO(((ParticipaUserDetails) authentication.getPrincipal()).getUsuario());
    }
}
