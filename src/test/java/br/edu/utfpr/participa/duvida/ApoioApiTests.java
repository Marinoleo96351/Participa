package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.aula.AulaRepository;
import br.edu.utfpr.participa.aula.SituacaoAula;
import br.edu.utfpr.participa.turma.Matricula;
import br.edu.utfpr.participa.turma.MatriculaRepository;
import br.edu.utfpr.participa.turma.Turma;
import br.edu.utfpr.participa.turma.TurmaRepository;
import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;
import br.edu.utfpr.participa.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApoioApiTests {

    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private DuvidaRepository duvidaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void segundoApoioDoMesmoEstudante_naoAumentaOTotal() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor1@alunos.utfpr.edu.br");
        Duvida duvida = enviarDuvida(aula, autor);
        Usuario apoiador = matricular(aula.getTurma(), "apoiador1@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(apoiador);

        apoiar(duvida, sessao).andExpect(status().isOk())
                .andExpect(jsonPath("$.apoios").value(1))
                .andExpect(jsonPath("$.apoiadaPorMim").value(true));

        apoiar(duvida, sessao).andExpect(status().isOk())
                .andExpect(jsonPath("$.apoios").value(1))
                .andExpect(jsonPath("$.apoiadaPorMim").value(true));
    }

    @Test
    void apoioNaPropriaDuvida_eRecusado() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor2@alunos.utfpr.edu.br");
        Duvida duvida = enviarDuvida(aula, autor);
        MockHttpSession sessao = logarComo(autor);

        apoiar(duvida, sessao).andExpect(status().isConflict());
    }

    @Test
    void respostaDoApoio_naoTrazQuemApoiou() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor3@alunos.utfpr.edu.br");
        Duvida duvida = enviarDuvida(aula, autor);
        Usuario apoiador = matricular(aula.getTurma(), "apoiador3@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(apoiador);

        apoiar(duvida, sessao).andExpect(status().isOk())
                .andExpect(jsonPath("$.apoios").exists())
                .andExpect(jsonPath("$.apoiadaPorMim").exists())
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.apoiadores").doesNotExist());
    }

    @Test
    void desapoiar_removeOApoioEDiminuiOTotal() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor4@alunos.utfpr.edu.br");
        Duvida duvida = enviarDuvida(aula, autor);
        Usuario apoiador = matricular(aula.getTurma(), "apoiador4@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(apoiador);

        apoiar(duvida, sessao).andExpect(status().isOk());

        mockMvc.perform(delete("/api/duvidas/" + duvida.getId() + "/apoio")
                        .session(sessao).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apoios").value(0))
                .andExpect(jsonPath("$.apoiadaPorMim").value(false));
    }

    @Test
    void estudanteDeOutraTurma_recebe403AoApoiar() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor5@alunos.utfpr.edu.br");
        Duvida duvida = enviarDuvida(aula, autor);
        Usuario estudanteDeFora = usuarioRepository.save(new Usuario(
                "De Fora", "defora5@alunos.utfpr.edu.br", passwordEncoder.encode(SENHA), Perfil.ESTUDANTE));
        MockHttpSession sessao = logarComo(estudanteDeFora);

        apoiar(duvida, sessao).andExpect(status().isForbidden());
    }

    private ResultActions apoiar(Duvida duvida, MockHttpSession sessao) throws Exception {
        return mockMvc.perform(post("/api/duvidas/" + duvida.getId() + "/apoio")
                .session(sessao).with(csrf()));
    }

    private Duvida enviarDuvida(Aula aula, Usuario autor) {
        return duvidaRepository.save(new Duvida(aula, autor, true, "Dúvida de teste.", StatusDuvida.PUBLICADA, false));
    }

    private Aula criarAulaAberta() {
        Usuario professor = usuarioRepository.save(new Usuario(
                "Professora", "professora" + System.nanoTime() + "@utfpr.edu.br",
                passwordEncoder.encode(SENHA), Perfil.PROFESSOR));
        Turma turma = turmaRepository.save(new Turma(
                "Turma de teste", codigoUnico(), professor, LocalDate.now().plusMonths(1)));
        return aulaRepository.save(new Aula(turma, 1, "Aula de teste", LocalDate.now(), SituacaoAula.ABERTA));
    }

    private String codigoUnico() {
        return "T" + (System.nanoTime() % 100000);
    }

    private Usuario matricular(Turma turma, String email) {
        Usuario estudante = usuarioRepository.save(new Usuario(
                "Estudante", email, passwordEncoder.encode(SENHA), Perfil.ESTUDANTE));
        matriculaRepository.save(new Matricula(turma, estudante));
        return estudante;
    }

    private MockHttpSession logarComo(Usuario usuario) throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String corpo = objectMapper.writeValueAsString(new LoginTeste(usuario.getEmail(), SENHA));
        mockMvc.perform(post("/api/auth/login")
                        .session(sessao).with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());
        return sessao;
    }

    private record LoginTeste(String email, String senha) {
    }
}
