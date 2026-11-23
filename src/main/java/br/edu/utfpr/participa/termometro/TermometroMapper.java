package br.edu.utfpr.participa.termometro;

import java.time.Instant;

import org.springframework.stereotype.Component;

import br.edu.utfpr.participa.termometro.dto.ResultadoTermometroDTO;
import br.edu.utfpr.participa.termometro.dto.TermometroDTO;

@Component
public class TermometroMapper {

    // Estudante: vê a própria resposta e NUNCA o resultado.
    public TermometroDTO toDTOEstudante(Termometro t, OpcaoResposta minhaResposta) {
        return new TermometroDTO(
                t.getId(),
                t.getNumero(),
                t.getAbertoEm(),
                t.getDuracaoSegundos(),
                t.estaAberto(Instant.now()),
                minhaResposta,
                null);
    }

    // Professor e monitor: veem o resultado e não têm "minhaResposta".
    public TermometroDTO toDTOProfessor(Termometro t, ResultadoTermometroDTO resultado) {
        return new TermometroDTO(
                t.getId(),
                t.getNumero(),
                t.getAbertoEm(),
                t.getDuracaoSegundos(),
                t.estaAberto(Instant.now()),
                null,
                resultado);
    }
}