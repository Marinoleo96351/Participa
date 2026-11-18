package br.edu.utfpr.participa.acesso;

import br.edu.utfpr.participa.turma.Turma;
import br.edu.utfpr.participa.turma.TurmaRepository;
import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;
import br.edu.utfpr.participa.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BaseAcessoApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SENHA = "senha123";

    @Test
    void cadastro_comEmailDeOutroDominio_eRecusado() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroTeste(
                "Fulano", "fulano@alunos.utfpr.edu.br", SENHA, "PROFESSOR"));

        mockMvc.perform(post("/api/auth/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastro_grava_senha_com_hash() throws Exception {
        String corpo = objectMapper.writeValueAsString(new CadastroTeste(
                "Fulana", "fulana@alunos.utfpr.edu.br", SENHA, "ESTUDANTE"));

        mockMvc.perform(post("/api/auth/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        Usuario usuario = usuarioRepository.findByEmail("fulana@alunos.utfpr.edu.br").orElseThrow();
        assertThat(usuario.getSenhaHash()).isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, usuario.getSenhaHash())).isTrue();
    }

    @Test
    void entrarTurma_comCodigoInexistente_devolve404ComMensagem() throws Exception {
        MockHttpSession sessao = logarComo(criarEstudante("estudante1@alunos.utfpr.edu.br"));

        mockMvc.perform(post("/api/turmas/entrar")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"ZZZZZZ\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Código não encontrado. Confira com o professor."));
    }

    @Test
    void abrirAula_estudanteRecebe403() throws Exception {
        MockHttpSession sessao = logarComo(criarEstudante("estudante2@alunos.utfpr.edu.br"));

        mockMvc.perform(post("/api/professor/turmas/1/aulas")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Aula qualquer\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void abrirSegundaAulaAberta_devolve409() throws Exception {
        Usuario professor = criarProfessor("professor1@utfpr.edu.br");
        Turma turma = turmaRepository.save(new Turma("Turma de teste", "ABC234", professor, LocalDate.now().plusMonths(1)));
        MockHttpSession sessao = logarComo(professor);

        String corpo = "{\"titulo\":\"Aula 1\"}";
        mockMvc.perform(post("/api/professor/turmas/" + turma.getId() + "/aulas")
                        .session(sessao).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/professor/turmas/" + turma.getId() + "/aulas")
                        .session(sessao).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void escritaSemCabecalhoCsrf_eRecusada() throws Exception {
        Usuario professor = criarProfessor("professor2@utfpr.edu.br");
        MockHttpSession sessao = logarComo(professor);

        mockMvc.perform(post("/api/professor/turmas")
                        .session(sessao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Turma sem CSRF\",\"fimSemestre\":\"2026-12-18\"}"))
                .andExpect(status().isForbidden());
    }

    private Usuario criarEstudante(String email) {
        return usuarioRepository.save(new Usuario("Estudante", email, passwordEncoder.encode(SENHA), Perfil.ESTUDANTE));
    }

    private Usuario criarProfessor(String email) {
        return usuarioRepository.save(new Usuario("Professor", email, passwordEncoder.encode(SENHA), Perfil.PROFESSOR));
    }

    private MockHttpSession logarComo(Usuario usuario) throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String corpo = objectMapper.writeValueAsString(new LoginTeste(usuario.getEmail(), SENHA));
        mockMvc.perform(post("/api/auth/login")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        return sessao;
    }

    private record CadastroTeste(String nome, String email, String senha, String perfil) {
    }

    private record LoginTeste(String email, String senha) {
    }
}
