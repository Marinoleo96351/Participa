package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.stereotype.Component;

@Component
public class DuvidaMapper {

    public DuvidaDTO toDTO(Duvida duvida, Usuario usuarioLogado) {
        boolean minha = duvida.getAutor().getId().equals(usuarioLogado.getId());
        return new DuvidaDTO(
                duvida.getId(),
                duvida.getTexto(),
                duvida.isAnonima(),
                duvida.isAnonima() ? null : duvida.getAutor().getNome(),
                minha,
                0,
                false,
                duvida.isRespondida(),
                null,
                duvida.isPosAula(),
                duvida.getStatus().name(),
                duvida.getTermoRetido(),
                duvida.getCriadaEm());
    }
}
