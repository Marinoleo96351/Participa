package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;
import br.edu.utfpr.participa.duvida.dto.EnviarDuvidaRequest;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DuvidaController {

    private final DuvidaService duvidaService;
    private final DuvidaMapper duvidaMapper;

    public DuvidaController(DuvidaService duvidaService, DuvidaMapper duvidaMapper) {
        this.duvidaService = duvidaService;
        this.duvidaMapper = duvidaMapper;
    }

    @PostMapping("/api/aulas/{aulaId}/duvidas")
    @ResponseStatus(HttpStatus.CREATED)
    DuvidaDTO enviar(@PathVariable Long aulaId, @RequestBody EnviarDuvidaRequest requisicao,
                      @AuthenticationPrincipal ParticipaUserDetails principal) {
        Duvida duvida = duvidaService.enviar(aulaId, requisicao.texto(), requisicao.anonima(), principal.getUsuario());
        return duvidaMapper.toDTO(duvida, principal.getUsuario(), 0, false);
    }
}
