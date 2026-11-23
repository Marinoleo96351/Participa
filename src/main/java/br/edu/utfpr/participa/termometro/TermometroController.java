package br.edu.utfpr.participa.termometro;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.aula.AulaService;
import br.edu.utfpr.participa.termometro.dto.AbrirTermometroRequest;
import br.edu.utfpr.participa.termometro.dto.ResponderTermometroRequest;
import br.edu.utfpr.participa.termometro.dto.TermometroDTO;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import br.edu.utfpr.participa.usuario.Perfil;
import jakarta.validation.Valid;

@RestController
public class TermometroController {

    private final TermometroService termometroService;
    private final TermometroMapper termometroMapper;
    private final AulaService aulaService;

    public TermometroController(TermometroService termometroService,
                                TermometroMapper termometroMapper,
                                AulaService aulaService) {
        this.termometroService = termometroService;
        this.termometroMapper = termometroMapper;
        this.aulaService = aulaService;
    }

    @PostMapping("/api/professor/aulas/{aulaId}/termometros")
    @ResponseStatus(HttpStatus.CREATED)
    TermometroDTO abrir(@PathVariable Long aulaId,
                        @Valid @RequestBody AbrirTermometroRequest requisicao,
                        @AuthenticationPrincipal ParticipaUserDetails principal) {
        Aula aula = aulaService.buscarEVerificarAcesso(aulaId, principal.getUsuario());
        Termometro t = termometroService.abrir(aula, requisicao.duracaoSegundos());
        return paraProfessor(t);
    }

    @PatchMapping("/api/professor/termometros/{id}/encerrar")
    TermometroDTO encerrar(@PathVariable Long id,
                           @AuthenticationPrincipal ParticipaUserDetails principal) {
        termometroService.buscarDoProfessor(id, principal.getUsuario());
        return paraProfessor(termometroService.encerrar(id));
    }

    @GetMapping("/api/professor/aulas/{aulaId}/termometros")
    List<TermometroDTO> listar(@PathVariable Long aulaId,
                               @AuthenticationPrincipal ParticipaUserDetails principal) {
        aulaService.buscarEVerificarAcesso(aulaId, principal.getUsuario());
        return termometroService.listarDaAula(aulaId).stream()
                .map(this::paraProfessor)
                .toList();
    }

    @PostMapping("/api/termometros/{id}/respostas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void responder(@PathVariable Long id,
                   @Valid @RequestBody ResponderTermometroRequest requisicao,
                   @AuthenticationPrincipal ParticipaUserDetails principal) {
        if (principal.getUsuario().getPerfil() != Perfil.ESTUDANTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Só estudantes respondem ao termômetro.");
        }
        termometroService.verificarMatricula(id, principal.getUsuario());
        termometroService.responder(id, principal.getUsuario(), requisicao.opcao());
    }

    private TermometroDTO paraProfessor(Termometro t) {
        return termometroMapper.toDTOProfessor(t, termometroService.resultado(t));
    }
}