package br.edu.utfpr.participa.usuario;

import br.edu.utfpr.participa.aula.AulaMapper;
import br.edu.utfpr.participa.aula.AulaService;
import br.edu.utfpr.participa.aula.dto.AulaDTO;
import br.edu.utfpr.participa.turma.TurmaMapper;
import br.edu.utfpr.participa.turma.TurmaService;
import br.edu.utfpr.participa.turma.dto.TurmaDTO;
import br.edu.utfpr.participa.usuario.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eu")
public class EuController {

    private final UsuarioMapper usuarioMapper;
    private final TurmaService turmaService;
    private final TurmaMapper turmaMapper;
    private final AulaService aulaService;
    private final AulaMapper aulaMapper;

    public EuController(UsuarioMapper usuarioMapper, TurmaService turmaService, TurmaMapper turmaMapper,
                         AulaService aulaService, AulaMapper aulaMapper) {
        this.usuarioMapper = usuarioMapper;
        this.turmaService = turmaService;
        this.turmaMapper = turmaMapper;
        this.aulaService = aulaService;
        this.aulaMapper = aulaMapper;
    }

    @GetMapping
    UsuarioDTO eu(@AuthenticationPrincipal ParticipaUserDetails principal) {
        return usuarioMapper.toDTO(principal.getUsuario());
    }

    @GetMapping("/turmas")
    List<TurmaDTO> turmas(@AuthenticationPrincipal ParticipaUserDetails principal) {
        Usuario usuario = principal.getUsuario();
        boolean doProfessor = usuario.getPerfil() == Perfil.PROFESSOR;
        return turmaService.listarDoUsuario(usuario).stream()
                .map(turma -> turmaMapper.toDTO(turma, doProfessor ? turmaService.contarEstudantes(turma) : null))
                .toList();
    }

    @GetMapping("/aula-atual")
    ResponseEntity<AulaDTO> aulaAtual(@AuthenticationPrincipal ParticipaUserDetails principal) {
        return aulaService.aulaAtual(principal.getUsuario())
                .map(aula -> ResponseEntity.ok(aulaMapper.toDTO(aula)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
