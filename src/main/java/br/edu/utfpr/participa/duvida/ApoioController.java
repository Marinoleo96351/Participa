package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.duvida.dto.ApoioDTO;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApoioController {

    private final ApoioService apoioService;

    public ApoioController(ApoioService apoioService) {
        this.apoioService = apoioService;
    }

    @PostMapping("/api/duvidas/{duvidaId}/apoio")
    ApoioDTO apoiar(@PathVariable Long duvidaId, @AuthenticationPrincipal ParticipaUserDetails principal) {
        return apoioService.apoiar(duvidaId, principal.getUsuario());
    }

    @DeleteMapping("/api/duvidas/{duvidaId}/apoio")
    ApoioDTO desapoiar(@PathVariable Long duvidaId, @AuthenticationPrincipal ParticipaUserDetails principal) {
        return apoioService.desapoiar(duvidaId, principal.getUsuario());
    }
}
