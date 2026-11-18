package br.edu.utfpr.participa.turma;

import br.edu.utfpr.participa.turma.dto.CriarTurmaRequest;
import br.edu.utfpr.participa.turma.dto.EntrarTurmaRequest;
import br.edu.utfpr.participa.turma.dto.TurmaDTO;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TurmaController {

    private final TurmaService turmaService;
    private final TurmaMapper turmaMapper;

    public TurmaController(TurmaService turmaService, TurmaMapper turmaMapper) {
        this.turmaService = turmaService;
        this.turmaMapper = turmaMapper;
    }

    @PostMapping("/api/turmas/entrar")
    TurmaDTO entrar(@Valid @RequestBody EntrarTurmaRequest requisicao,
                     @AuthenticationPrincipal ParticipaUserDetails principal) {
        Turma turma = turmaService.entrar(requisicao.codigo(), principal.getUsuario());
        return turmaMapper.toDTO(turma, null);
    }

    @GetMapping("/api/professor/turmas")
    List<TurmaDTO> minhasTurmas(@AuthenticationPrincipal ParticipaUserDetails principal) {
        return turmaService.listarDoProfessor(principal.getUsuario()).stream()
                .map(turma -> turmaMapper.toDTO(turma, turmaService.contarEstudantes(turma)))
                .toList();
    }

    @PostMapping("/api/professor/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    TurmaDTO criar(@Valid @RequestBody CriarTurmaRequest requisicao,
                    @AuthenticationPrincipal ParticipaUserDetails principal) {
        Turma turma = turmaService.criar(requisicao.nome(), requisicao.fimSemestre(), principal.getUsuario());
        return turmaMapper.toDTO(turma, 0);
    }
}
