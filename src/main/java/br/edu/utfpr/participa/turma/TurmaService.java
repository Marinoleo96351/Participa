package br.edu.utfpr.participa.turma;

import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
public class TurmaService {

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 6;

    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final SecureRandom sorteador = new SecureRandom();

    public TurmaService(TurmaRepository turmaRepository, MatriculaRepository matriculaRepository) {
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public Turma criar(String nome, LocalDate fimSemestre, Usuario professor) {
        Turma turma = new Turma(nome, gerarCodigo(), professor, fimSemestre);
        return turmaRepository.save(turma);
    }

    private String gerarCodigo() {
        String codigo;
        do {
            codigo = sortearCodigo();
        } while (turmaRepository.existsByCodigoAcesso(codigo));
        return codigo;
    }

    private String sortearCodigo() {
        StringBuilder codigo = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            codigo.append(ALFABETO_CODIGO.charAt(sorteador.nextInt(ALFABETO_CODIGO.length())));
        }
        return codigo.toString();
    }

    public Turma entrar(String codigo, Usuario estudante) {
        Turma turma = turmaRepository.findByCodigoAcesso(codigo.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Código não encontrado. Confira com o professor."));
        if (!matriculaRepository.existsById_TurmaIdAndId_UsuarioId(turma.getId(), estudante.getId())) {
            matriculaRepository.save(new Matricula(turma, estudante));
        }
        return turma;
    }

    public List<Turma> listarDoProfessor(Usuario professor) {
        return turmaRepository.findByProfessorIdOrderByNomeAsc(professor.getId());
    }

    public List<Turma> listarDoUsuario(Usuario usuario) {
        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            return listarDoProfessor(usuario);
        }
        return matriculaRepository.findById_UsuarioIdOrderByTurma_NomeAsc(usuario.getId()).stream()
                .map(Matricula::getTurma)
                .toList();
    }

    public List<Long> idsDasTurmasDoUsuario(Usuario usuario) {
        return listarDoUsuario(usuario).stream().map(Turma::getId).toList();
    }

    public int contarEstudantes(Turma turma) {
        return (int) matriculaRepository.countById_TurmaId(turma.getId());
    }

    public Turma buscarDoProfessor(Long turmaId, Usuario professor) {
        return turmaRepository.findByIdAndProfessorId(turmaId, professor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
    }
}
