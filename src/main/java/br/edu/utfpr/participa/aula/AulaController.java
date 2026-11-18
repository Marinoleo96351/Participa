package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.aula.dto.AbrirAulaRequest;
import br.edu.utfpr.participa.aula.dto.AulaDTO;
import br.edu.utfpr.participa.aula.dto.EncerrarAulaRequest;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AulaController {

    private final AulaService aulaService;
    private final AulaMapper aulaMapper;

    public AulaController(AulaService aulaService, AulaMapper aulaMapper) {
        this.aulaService = aulaService;
        this.aulaMapper = aulaMapper;
    }

    @GetMapping("/api/professor/turmas/{turmaId}/aulas")
    List<AulaDTO> listar(@PathVariable Long turmaId, @AuthenticationPrincipal ParticipaUserDetails principal) {
        return aulaService.listarDaTurma(turmaId, principal.getUsuario()).stream()
                .map(aulaMapper::toDTO)
                .toList();
    }

    @PostMapping("/api/professor/turmas/{turmaId}/aulas")
    @ResponseStatus(HttpStatus.CREATED)
    AulaDTO abrir(@PathVariable Long turmaId, @Valid @RequestBody AbrirAulaRequest requisicao,
                   @AuthenticationPrincipal ParticipaUserDetails principal) {
        Aula aula = aulaService.abrir(turmaId, requisicao.titulo(), principal.getUsuario());
        return aulaMapper.toDTO(aula);
    }

    @PatchMapping("/api/professor/aulas/{id}/encerrar")
    AulaDTO encerrar(@PathVariable Long id, @Valid @RequestBody EncerrarAulaRequest requisicao,
                      @AuthenticationPrincipal ParticipaUserDetails principal) {
        Aula aula = aulaService.encerrar(id, requisicao.opcao(), principal.getUsuario());
        return aulaMapper.toDTO(aula);
    }
}
