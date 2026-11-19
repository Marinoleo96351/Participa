package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.stereotype.Component;

@Component
public class DuvidaMapper {

    public DuvidaDTO toDTO(Duvida duvida, Usuario usuarioLogado, int apoios, boolean apoiadaPorMim) {
        boolean minha = duvida.getAutor().getId().equals(usuarioLogado.getId());
        return new DuvidaDTO(
                duvida.getId(),
                duvida.getTexto(),
                duvida.isAnonima(),
                duvida.isAnonima() ? null : duvida.getAutor().getNome(),
                minha,
                apoios,
                apoiadaPorMim,
                duvida.isRespondida(),
                null,
                duvida.isPosAula(),
                duvida.getStatus().name(),
                duvida.getTermoRetido(),
                duvida.getCriadaEm());
    }
}
