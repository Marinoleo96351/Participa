package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.turma.Turma;
import br.edu.utfpr.participa.turma.TurmaService;
import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AulaService {

    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final List<SituacaoAula> SITUACOES_ATIVAS = List.of(SituacaoAula.ABERTA, SituacaoAula.POS_AULA);

    private final AulaRepository aulaRepository;
    private final TurmaService turmaService;

    public AulaService(AulaRepository aulaRepository, TurmaService turmaService) {
        this.aulaRepository = aulaRepository;
        this.turmaService = turmaService;
    }

    public List<Aula> listarDaTurma(Long turmaId, Usuario professor) {
        Turma turma = turmaService.buscarDoProfessor(turmaId, professor);
        return aulaRepository.findByTurmaIdOrderByNumeroDesc(turma.getId());
    }

    public Aula abrir(Long turmaId, String titulo, Usuario professor) {
        Turma turma = turmaService.buscarDoProfessor(turmaId, professor);
        if (aulaRepository.existsByTurmaIdAndSituacao(turma.getId(), SituacaoAula.ABERTA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma aula aberta nesta turma.");
        }
        int numero = aulaRepository.buscarMaiorNumero(turma.getId()).orElse(0) + 1;
        Aula aula = new Aula(turma, numero, titulo, LocalDate.now(FUSO_PADRAO), SituacaoAula.ABERTA);
        return aulaRepository.save(aula);
    }

    public Aula encerrar(Long aulaId, String opcao, Usuario professor) {
        Aula aula = aulaRepository.findByIdAndTurma_Professor_Id(aulaId, professor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
        if (aula.getSituacao() != SituacaoAula.ABERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta aula já foi encerrada.");
        }
        Instant agora = Instant.now();
        aula.encerrar(calcularPrazo(opcao, agora), agora);
        return aulaRepository.save(aula);
    }

    private Instant calcularPrazo(String opcao, Instant agora) {
        return switch (opcao) {
            case "24" -> agora.plus(24, ChronoUnit.HOURS);
            case "48" -> agora.plus(48, ChronoUnit.HOURS);
            case "proxima" -> agora.atZone(FUSO_PADRAO).plusDays(7).toLocalDate()
                    .atTime(23, 59).atZone(FUSO_PADRAO).toInstant();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opção de prazo inválida.");
        };
    }

    public Aula buscarEVerificarAcesso(Long aulaId, Usuario usuario) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
        verificarAcesso(aula, usuario);
        return aula;
    }

    private void verificarAcesso(Aula aula, Usuario usuario) {
        boolean temAcesso = switch (usuario.getPerfil()) {
            case PROFESSOR -> aula.getTurma().getProfessor().getId().equals(usuario.getId());
            case ESTUDANTE -> turmaService.estaMatriculado(aula.getTurma().getId(), usuario.getId());
            case MONITOR -> false;
        };
        if (!temAcesso) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta aula.");
        }
    }

    public Optional<Aula> aulaAtual(Usuario usuario) {
        List<Long> turmaIds = turmaService.idsDasTurmasDoUsuario(usuario);
        if (turmaIds.isEmpty()) {
            return Optional.empty();
        }
        return aulaRepository.buscarAtivasDasTurmas(turmaIds, SITUACOES_ATIVAS).stream().findFirst();
    }
}
