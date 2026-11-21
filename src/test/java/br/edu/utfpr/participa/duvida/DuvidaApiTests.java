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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private DuvidaRepository duvidaRepository;

    @Autowired
    private ApoioRepository apoioRepository;

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

    @Test
    void duvidaComMaisApoios_apareceAntesNaLista() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor9@alunos.utfpr.edu.br");
        Duvida poucoApoio = criarDuvida(aula, autor, "Dúvida com pouco apoio.");
        Duvida muitoApoio = criarDuvida(aula, autor, "Dúvida com muito apoio.");
        apoiar(muitoApoio, 2);
        apoiar(poucoApoio, 1);

        MockHttpSession sessao = logarComo(aula.getTurma().getProfessor());
        mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duvidas[0].texto").value(muitoApoio.getTexto()))
                .andExpect(jsonPath("$.duvidas[1].texto").value(poucoApoio.getTexto()));
    }

    @Test
    void empateDeApoios_colocaAMaisAntigaPrimeiro() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor10@alunos.utfpr.edu.br");
        Duvida antiga = criarDuvida(aula, autor, "Dúvida mais antiga.");
        Thread.sleep(5);
        Duvida nova = criarDuvida(aula, autor, "Dúvida mais nova.");
        apoiar(antiga, 1);
        apoiar(nova, 1);

        MockHttpSession sessao = logarComo(aula.getTurma().getProfessor());
        mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duvidas[0].texto").value(antiga.getTexto()))
                .andExpect(jsonPath("$.duvidas[1].texto").value(nova.getTexto()));
    }

    @Test
    void duvidaRespondida_vaiParaOFimDaLista() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor11@alunos.utfpr.edu.br");
        Duvida respondidaComMaisApoio = criarDuvida(aula, autor, "Dúvida respondida com mais apoio.");
        Duvida pendenteComMenosApoio = criarDuvida(aula, autor, "Dúvida pendente com menos apoio.");
        apoiar(respondidaComMaisApoio, 3);
        apoiar(pendenteComMenosApoio, 1);
        respondidaComMaisApoio.marcarRespondida(true);
        duvidaRepository.save(respondidaComMaisApoio);

        MockHttpSession sessao = logarComo(aula.getTurma().getProfessor());
        mockMvc.perform(get("/api/aulas/" + aula.getId() + "/estado").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duvidas[0].texto").value(pendenteComMenosApoio.getTexto()))
                .andExpect(jsonPath("$.duvidas[1].texto").value(respondidaComMaisApoio.getTexto()));
    }

    @Test
    void professorMarcaEDesmarcaRespondida() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor12@alunos.utfpr.edu.br");
        Duvida duvida = criarDuvida(aula, autor, "Dúvida para marcar.");
        MockHttpSession sessao = logarComo(aula.getTurma().getProfessor());

        marcarRespondida(duvida, sessao, true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respondida").value(true));

        marcarRespondida(duvida, sessao, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respondida").value(false));
    }

    @Test
    void estudanteRecebe403AoMarcarRespondida() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor13@alunos.utfpr.edu.br");
        Duvida duvida = criarDuvida(aula, autor, "Dúvida qualquer.");
        MockHttpSession sessao = logarComo(autor);

        marcarRespondida(duvida, sessao, true)
                .andExpect(status().isForbidden());
    }

    @Test
    void professorDeOutraTurma_recebe403AoMarcarRespondida() throws Exception {
        Aula aula = criarAulaAberta();
        Usuario autor = matricular(aula.getTurma(), "autor14@alunos.utfpr.edu.br");
        Duvida duvida = criarDuvida(aula, autor, "Dúvida de outra turma.");
        Usuario professorDeFora = usuarioRepository.save(new Usuario(
                "Outro Professor", "outroprofessor" + System.nanoTime() + "@utfpr.edu.br",
                passwordEncoder.encode(SENHA), Perfil.PROFESSOR));
        MockHttpSession sessao = logarComo(professorDeFora);

        marcarRespondida(duvida, sessao, true)
                .andExpect(status().isForbidden());
    }

    private ResultActions marcarRespondida(Duvida duvida, MockHttpSession sessao, boolean respondida) throws Exception {
        String corpo = objectMapper.writeValueAsString(new MarcarRespondidaTeste(respondida));
        return mockMvc.perform(patch("/api/professor/duvidas/" + duvida.getId() + "/respondida")
                .session(sessao).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Duvida criarDuvida(Aula aula, Usuario autor, String texto) {
        return duvidaRepository.save(new Duvida(aula, autor, false, texto, StatusDuvida.PUBLICADA, false));
    }

    private void apoiar(Duvida duvida, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Usuario apoiador = usuarioRepository.save(new Usuario(
                    "Apoiador", "apoiador" + System.nanoTime() + "@alunos.utfpr.edu.br",
                    passwordEncoder.encode(SENHA), Perfil.ESTUDANTE));
            apoioRepository.save(new Apoio(duvida, apoiador));
        }
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

    private record MarcarRespondidaTeste(boolean respondida) {
    }
}
