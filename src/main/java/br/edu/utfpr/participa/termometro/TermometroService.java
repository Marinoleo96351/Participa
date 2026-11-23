package br.edu.utfpr.participa.termometro;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.usuario.Usuario;

@Service
public class TermometroService {

    private final TermometroRepository termometros;
    private final RespostaTermometroRepository respostas;

    public TermometroService(TermometroRepository termometros,
                             RespostaTermometroRepository respostas) {
        this.termometros = termometros;
        this.respostas = respostas;
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
        Termometro t = buscar(termometroId);

        Instant agora = Instant.now();
        if (t.estaAberto(agora)) {
            t.encerrar(agora);
        }
        return t;
    }

    @Transactional
    public void responder(Long termometroId, Usuario usuario, OpcaoResposta opcao) {
        Termometro t = buscar(termometroId);

        if (!t.estaAberto(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este termômetro já foi encerrado.");
        }
        if (respostas.existsById_TermometroIdAndId_UsuarioId(termometroId, usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você já respondeu a este termômetro.");
        }

        respostas.save(new RespostaTermometro(t, usuario, opcao));
    }

    private Termometro buscar(Long id) {
        return termometros.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Termômetro não encontrado."));
    }
}