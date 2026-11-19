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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DuvidaApiTests {

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
    private PasswordEncoder passwordEncoder;

    @Test
    void textoComMaisDe500Caracteres_eRecusado() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario estudante = matricular(aula.getTurma(), "estudante1@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(estudante);

        String textoGrande = "a".repeat(501);
        enviarDuvida(aula, sessao, textoGrande, true)
                .andExpect(status().isBadRequest());
    }

    @Test
    void textoComApenasEspacos_eRecusado() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario estudante = matricular(aula.getTurma(), "estudante2@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(estudante);

        enviarDuvida(aula, sessao, "     ", true)
                .andExpect(status().isBadRequest());
    }

    @Test
    void duvidaAnonima_naoTrazAutorNomeNemParaProfessor() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario estudante = matricular(aula.getTurma(), "estudante3@alunos.utfpr.edu.br");
        MockHttpSession sessaoEstudante = logarComo(estudante);
        enviarDuvida(aula, sessaoEstudante, "Não entendi a rotação dupla.", true)
                .andExpect(status().isCreated());

        MockHttpSession sessaoProfessor = logarComo(aula.getTurma().getProfessor());
        mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado").session(sessaoProfessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duvidas[0].anonima").value(true))
                .andExpect(jsonPath("$.duvidas[0].autorNome").doesNotExist());
    }

    @Test
    void estudanteDeOutraTurma_recebe403() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario estudanteDeFora = usuarioRepository.save(new Usuario(
                "De Fora", "defora@alunos.utfpr.edu.br", passwordEncoder.encode(SENHA), Perfil.ESTUDANTE));
        MockHttpSession sessao = logarComo(estudanteDeFora);

        enviarDuvida(aula, sessao, "Dúvida de quem não está matriculado.", false)
                .andExpect(status().isForbidden());
    }

    @Test
    void rotaDeEstado_responde304ComMesmoIfNoneMatch() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario professor = aula.getTurma().getProfessor();
        MockHttpSession sessao = logarComo(professor);

        MvcResult primeira = mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado").session(sessao))
                .andExpect(status().isOk())
                .andReturn();
        String etag = primeira.getResponse().getHeader(HttpHeaders.ETAG);

        mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado")
                        .session(sessao)
                        .header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
    }

    @Test
    void envioEmPosAulaDentroDoPrazo_eAceitoEMarcadoComoPosAula() throws Exception {
        Aula aula = criarAulaPosAula(Instant.now().plus(1, ChronoUnit.HOURS));
        Usuario estudante = matricular(aula.getTurma(), "estudante4@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(estudante);

        enviarDuvida(aula, sessao, "Dúvida enviada depois da aula.", false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.posAula").value(true));
    }

    @Test
    void envioEmPosAulaDepoisDoPrazo_eRecusadoCom409EMensagemComAData() throws Exception {
        Aula aula = criarAulaPosAula(Instant.now().minus(1, ChronoUnit.HOURS));
        Usuario estudante = matricular(aula.getTurma(), "estudante5@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(estudante);

        enviarDuvida(aula, sessao, "Dúvida tardia.", false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", Matchers.containsString("terminou em")));
    }

    @Test
    void envioComAulaEncerrada_eRecusadoCom409() throws Exception {
        Aula aula = criarAulaEncerrada();
        Usuario estudante = matricular(aula.getTurma(), "estudante6@alunos.utfpr.edu.br");
        MockHttpSession sessao = logarComo(estudante);

        enviarDuvida(aula, sessao, "Dúvida depois do fim.", false)
                .andExpect(status().isConflict());
    }

    private ResultActions enviarDuvida(
            Aula aula, MockHttpSession sessao, String texto, boolean anonima) throws Exception {
        String corpo = objectMapper.writeValueAsString(new EnviarDuvidaTeste(texto, anonima));
        return mockMvc.perform(post("/api/aulas/" + aula.getId() + "/duvidas")
                .session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Aula criarAulaAberta() {
        return criarAulaComSituacao(SituacaoAula.ABERTA);
    }

    private Aula criarAulaPosAula(Instant prazo) {
        Aula aula = criarAulaComSituacao(SituacaoAula.ABERTA);
        aula.encerrar(prazo, Instant.now());
        return aulaRepository.save(aula);
    }

    private Aula criarAulaEncerrada() {
        return criarAulaComSituacao(SituacaoAula.ENCERRADA);
    }

    private Aula criarAulaComSituacao(SituacaoAula situacao) {
        Usuario professor = usuarioRepository.save(new Usuario(
                "Professora", "professora" + System.nanoTime() + "@utfpr.edu.br",
                passwordEncoder.encode(SENHA), Perfil.PROFESSOR));
        Turma turma = turmaRepository.save(new Turma(
                "Turma de teste", codigoUnico(), professor, LocalDate.now().plusMonths(1)));
        return aulaRepository.save(new Aula(turma, 1, "Aula de teste", LocalDate.now(), situacao));
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
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());
        return sessao;
    }

    private record EnviarDuvidaTeste(String texto, boolean anonima) {
    }

    private record LoginTeste(String email, String senha) {
    }
}
