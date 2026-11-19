package br.edu.utfpr.participa.aula;

import br.edu.utfpr.participa.aula.dto.EstadoAulaDTO;
import br.edu.utfpr.participa.duvida.DuvidaService;
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
    private final ObjectMapper objectMapper;

    public EstadoAulaService(AulaService aulaService, AulaMapper aulaMapper, DuvidaService duvidaService,
                              ObjectMapper objectMapper) {
        this.aulaService = aulaService;
        this.aulaMapper = aulaMapper;
        this.duvidaService = duvidaService;
        this.objectMapper = objectMapper;
    }

    public EstadoAulaDTO buscar(Long aulaId, Usuario usuario) {
        Aula aula = aulaService.buscarEVerificarAcesso(aulaId, usuario);
        return new EstadoAulaDTO(
                aulaMapper.toDTO(aula),
                duvidaService.listarVisiveis(aula, usuario),
                null,
                Instant.now());
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
