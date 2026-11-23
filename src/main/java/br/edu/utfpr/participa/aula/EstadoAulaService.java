package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.aula.dto.EstadoAulaDTO;
import br.edu.utfpr.participa.duvida.DuvidaService;
import br.edu.utfpr.participa.termometro.Termometro;
import br.edu.utfpr.participa.termometro.TermometroMapper;
import br.edu.utfpr.participa.termometro.TermometroService;
import br.edu.utfpr.participa.termometro.dto.TermometroDTO;
import br.edu.utfpr.participa.usuario.Perfil;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class EstadoAulaService {

    private final AulaService aulaService;
    private final AulaMapper aulaMapper;
    private final DuvidaService duvidaService;
    private final TermometroService termometroService;
    private final TermometroMapper termometroMapper;
    private final ObjectMapper objectMapper;

    public EstadoAulaService(AulaService aulaService, AulaMapper aulaMapper, DuvidaService duvidaService,
                              TermometroService termometroService, TermometroMapper termometroMapper,
                              ObjectMapper objectMapper) {
        this.aulaService = aulaService;
        this.aulaMapper = aulaMapper;
        this.duvidaService = duvidaService;
        this.termometroService = termometroService;
        this.termometroMapper = termometroMapper;
        this.objectMapper = objectMapper;
    }

    public EstadoAulaDTO buscar(Long aulaId, Usuario usuario) {
        Aula aula = aulaService.buscarEVerificarAcesso(aulaId, usuario);
        return new EstadoAulaDTO(
                aulaMapper.toDTO(aula),
                duvidaService.listarVisiveis(aula, usuario),
                termometroAtivo(aula, usuario),
                Instant.now());
    }

    private TermometroDTO termometroAtivo(Aula aula, Usuario usuario) {
        return termometroService.ultimoDaAula(aula.getId())
                .map(t -> montarDTO(t, usuario))
                .orElse(null);
    }

    private TermometroDTO montarDTO(Termometro t, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            return termometroMapper.toDTOProfessor(t, termometroService.resultado(t));
        }
        return termometroMapper.toDTOEstudante(t,
                termometroService.minhaResposta(t.getId(), usuario.getId()));
    }

    // Zera servidorAgora antes de calcular: ele muda a cada chamada e invalidaria qualquer ETag do corpo inteiro.
    public String calcularEtag(EstadoAulaDTO estado) {
        EstadoAulaDTO semRelogio = new EstadoAulaDTO(estado.aula(), estado.duvidas(), estado.termometro(), Instant.EPOCH);
        byte[] bytes = objectMapper.writeValueAsBytes(semRelogio);
        return sha256(bytes);
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hexadecimal = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}