package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.aula.dto.AulaDTO;
import org.springframework.stereotype.Component;

@Component
public class AulaMapper {

    public AulaDTO toDTO(Aula aula) {
        return new AulaDTO(
                aula.getId(),
                aula.getTurma().getId(),
                aula.getNumero(),
                aula.getTitulo(),
                aula.getData(),
                aula.getSituacao().name(),
                aula.getPrazoPosAula(),
                null);
    }
}
