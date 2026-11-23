package br.edu.utfpr.participa.termometro;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.utfpr.participa.aula.Aula;

@Service
public class TermometroService {

    private final TermometroRepository termometros;

    public TermometroService(TermometroRepository termometros) {
        this.termometros = termometros;
    }

    @Transactional
    public Termometro abrir(Aula aula, int duracaoSegundos) {
        if (duracaoSegundos < 15 || duracaoSegundos > 600) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A duração deve ficar entre 15 e 600 segundos.");
        }

        var ultimo = termometros.findFirstByAula_IdOrderByNumeroDesc(aula.getId());

        if (ultimo.isPresent() && ultimo.get().estaAberto(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um termômetro aberto nesta aula.");
        }

        int proximoNumero = ultimo.map(t -> t.getNumero() + 1).orElse(1);
        return termometros.save(new Termometro(aula, proximoNumero, duracaoSegundos));
    }

    @Transactional
    public Termometro encerrar(Long termometroId) {
        Termometro t = termometros.findById(termometroId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Termômetro não encontrado."));

        Instant agora = Instant.now();
        if (t.estaAberto(agora)) {
            t.encerrar(agora);
        }
        return t;
    }
}