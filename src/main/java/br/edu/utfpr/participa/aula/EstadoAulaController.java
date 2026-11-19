package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.aula.dto.EstadoAulaDTO;
import br.edu.utfpr.participa.usuario.ParticipaUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadoAulaController {

    private final EstadoAulaService estadoAulaService;

    public EstadoAulaController(EstadoAulaService estadoAulaService) {
        this.estadoAulaService = estadoAulaService;
    }

    @GetMapping("/api/aulas/{id}/estado")
    ResponseEntity<EstadoAulaDTO> estado(@PathVariable Long id,
                                          @AuthenticationPrincipal ParticipaUserDetails principal,
                                          @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        EstadoAulaDTO estado = estadoAulaService.buscar(id, principal.getUsuario());
        String etag = estadoAulaService.calcularEtag(estado);
        if (etag.equals(semAspas(ifNoneMatch))) {
            return ResponseEntity.status(304).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).body(estado);
    }

    private String semAspas(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replace("\"", "").trim();
    }
}
