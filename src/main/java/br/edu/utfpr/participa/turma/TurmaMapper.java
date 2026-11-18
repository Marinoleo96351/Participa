package br.edu.utfpr.participa.turma;

import br.edu.utfpr.participa.turma.dto.TurmaDTO;
import org.springframework.stereotype.Component;

@Component
public class TurmaMapper {

    public TurmaDTO toDTO(Turma turma, Integer estudantes) {
        return new TurmaDTO(
                turma.getId(),
                turma.getNome(),
                turma.getCodigoAcesso(),
                turma.getFimSemestre(),
                estudantes);
    }
}
