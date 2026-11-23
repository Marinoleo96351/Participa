package br.edu.utfpr.participa.termometro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.aula.SituacaoAula;
import br.edu.utfpr.participa.termometro.dto.ResultadoTermometroDTO;
import br.edu.utfpr.participa.termometro.dto.TermometroDTO;
import br.edu.utfpr.participa.turma.MatriculaRepository;
import br.edu.utfpr.participa.turma.Turma;
import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class TermometroServiceTest {

    @Mock
    private TermometroRepository termometros;

    @Mock
    private RespostaTermometroRepository respostas;

    @Mock
    private MatriculaRepository matriculas;

    private TermometroService service;
    private Usuario professor;
    private Usuario estudante;
    private Turma turma;
    private Aula aula;

    @BeforeEach
    void preparar() {
        service = new TermometroService(termometros, respostas, matriculas);

        professor = new Usuario("Professor", "prof@utfpr.edu.br", "hash", Perfil.PROFESSOR);
        estudante = new Usuario("Aluno", "aluno@alunos.utfpr.edu.br", "hash", Perfil.ESTUDANTE);
        ReflectionTestUtils.setField(professor, "id", 1L);
        ReflectionTestUtils.setField(estudante, "id", 2L);

        turma = new Turma("Estruturas de Dados", "K7M2QX", professor, LocalDate.now().plusMonths(3));
        ReflectionTestUtils.setField(turma, "id", 10L);

        aula = new Aula(turma, 12, "Árvores AVL", LocalDate.now(), SituacaoAula.ABERTA);
        ReflectionTestUtils.setField(aula, "id", 100L);
    }

    // Cria um termômetro de 60 s que foi aberto há "segundosAtras" segundos.
    private Termometro termometroAberto(long segundosAtras) {
        Termometro t = new Termometro(aula, 1, 60);
        ReflectionTestUtils.setField(t, "id", 500L);
        ReflectionTestUtils.setField(t, "abertoEm", Instant.now().minusSeconds(segundosAtras));
        return t;
    }

    private static RespostaTermometroRepository.ContagemOpcao contagem(OpcaoResposta opcao, long total) {
        return new RespostaTermometroRepository.ContagemOpcao() {
            @Override
            public OpcaoResposta getOpcao() {
                return opcao;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }

    private static ResponseStatusException conflito(Runnable acao) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(409, erro.getStatusCode().value());
        return erro;
    }

    @Test
    void respostaDepoisDoPrazoERecusada() {
        Termometro vencido = termometroAberto(120); // durava 60 s, abriu há 120 s
        when(termometros.findById(500L)).thenReturn(Optional.of(vencido));

        conflito(() -> service.responder(500L, estudante, OpcaoResposta.ENTENDI));

        verify(respostas, never()).save(any());
    }

    @Test
    void segundaRespostaDoMesmoEstudanteERecusada() {
        Termometro aberto = termometroAberto(5);
        when(termometros.findById(500L)).thenReturn(Optional.of(aberto));
        when(respostas.existsById_TermometroIdAndId_UsuarioId(500L, 2L)).thenReturn(true);

        conflito(() -> service.responder(500L, estudante, OpcaoResposta.PARCIAL));

        verify(respostas, never()).save(any());
    }

    @Test
    void primeiraRespostaNoPrazoEGravada() {
        Termometro aberto = termometroAberto(5);
        when(termometros.findById(500L)).thenReturn(Optional.of(aberto));
        when(respostas.existsById_TermometroIdAndId_UsuarioId(500L, 2L)).thenReturn(false);

        service.responder(500L, estudante, OpcaoResposta.NAO_ENTENDI);

        verify(respostas).save(any(RespostaTermometro.class));
    }

    @Test
    void segundoTermometroAbertoNaMesmaAulaERecusado() {
        Termometro aberto = termometroAberto(5);
        when(termometros.findFirstByAula_IdOrderByNumeroDesc(100L)).thenReturn(Optional.of(aberto));

        conflito(() -> service.abrir(aula, 60));

        verify(termometros, never()).save(any());
    }

    @Test
    void duracaoForaDoLimiteERecusada() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.abrir(aula, 10));
        assertEquals(400, erro.getStatusCode().value());
    }

    @Test
    void resultadoSomaAsOpcoesSemIdentificarNinguem() {
        Termometro t = termometroAberto(5);
        when(respostas.contarPorOpcao(500L)).thenReturn(List.of(
                contagem(OpcaoResposta.ENTENDI, 3),
                contagem(OpcaoResposta.PARCIAL, 1),
                contagem(OpcaoResposta.NAO_ENTENDI, 2)));
        when(matriculas.countById_TurmaId(10L)).thenReturn(8L);

        ResultadoTermometroDTO resultado = service.resultado(t);

        assertEquals(3, resultado.entendi());
        assertEquals(1, resultado.parcial());
        assertEquals(2, resultado.naoEntendi());
        assertEquals(6, resultado.respostas());
        assertEquals(8, resultado.matriculados());

        // O resultado só pode ter números: nenhum campo de usuário.
        Set<String> campos = Stream.of(ResultadoTermometroDTO.class.getRecordComponents())
                .map(c -> c.getName())
                .collect(Collectors.toSet());
        assertEquals(Set.of("entendi", "parcial", "naoEntendi", "respostas", "matriculados"), campos);
    }

    @Test
    void estudanteNaoRecebeResultadoEProfessorNaoRecebeMinhaResposta() {
        TermometroMapper mapper = new TermometroMapper();
        Termometro t = termometroAberto(5);

        TermometroDTO paraEstudante = mapper.toDTOEstudante(t, OpcaoResposta.ENTENDI);
        assertNull(paraEstudante.resultado());
        assertEquals(OpcaoResposta.ENTENDI, paraEstudante.minhaResposta());

        TermometroDTO paraProfessor = mapper.toDTOProfessor(t, new ResultadoTermometroDTO(1, 0, 0, 1, 5));
        assertNull(paraProfessor.minhaResposta());
        assertTrue(paraProfessor.resultado() != null);
    }
}
