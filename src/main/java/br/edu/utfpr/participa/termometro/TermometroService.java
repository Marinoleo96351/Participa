package br.edu.utfpr.participa.termometro;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.termometro.dto.ResultadoTermometroDTO;
import br.edu.utfpr.participa.turma.MatriculaRepository;
import br.edu.utfpr.participa.usuario.Usuario;

@Service
public class TermometroService {

    private final TermometroRepository termometros;
    private final RespostaTermometroRepository respostas;
    private final MatriculaRepository matriculas;

    public TermometroService(TermometroRepository termometros,
                             RespostaTermometroRepository respostas,
                             MatriculaRepository matriculas) {
        this.termometros = termometros;
        this.respostas = respostas;
        this.matriculas = matriculas;
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

    @Transactional(readOnly = true)
    public ResultadoTermometroDTO resultado(Termometro t) {
        long entendi = 0;
        long parcial = 0;
        long naoEntendi = 0;

        for (var contagem : respostas.contarPorOpcao(t.getId())) {
            switch (contagem.getOpcao()) {
                case ENTENDI -> entendi = contagem.getTotal();
                case PARCIAL -> parcial = contagem.getTotal();
                case NAO_ENTENDI -> naoEntendi = contagem.getTotal();
            }
        }

        long matriculados = matriculas.countById_TurmaId(t.getAula().getTurma().getId());

        return new ResultadoTermometroDTO(
                entendi, parcial, naoEntendi,
                entendi + parcial + naoEntendi,
                matriculados);
    }

    @Transactional(readOnly = true)
    public Termometro buscarDoProfessor(Long termometroId, Usuario professor) {
        Termometro t = buscar(termometroId);
        Long donoId = t.getAula().getTurma().getProfessor().getId();
        if (!donoId.equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Termômetro não encontrado.");
        }
        return t;
    }

    @Transactional(readOnly = true)
    public void verificarMatricula(Long termometroId, Usuario estudante) {
        Termometro t = buscar(termometroId);
        Long turmaId = t.getAula().getTurma().getId();
        if (!matriculas.existsById_TurmaIdAndId_UsuarioId(turmaId, estudante.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem acesso a esta aula.");
        }
    }

    @Transactional(readOnly = true)
    public List<Termometro> listarDaAula(Long aulaId) {
        return termometros.findByAula_IdOrderByNumeroAsc(aulaId);
    }

    @Transactional(readOnly = true)
    public Optional<Termometro> ultimoDaAula(Long aulaId) {
        return termometros.findFirstByAula_IdOrderByNumeroDesc(aulaId);
    }

    @Transactional(readOnly = true)
    public OpcaoResposta minhaResposta(Long termometroId, Long usuarioId) {
        return respostas.findById_TermometroIdAndId_UsuarioId(termometroId, usuarioId)
                .map(RespostaTermometro::getOpcao)
                .orElse(null);
    }

    private Termometro buscar(Long id) {
        return termometros.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Termômetro não encontrado."));
    }
}