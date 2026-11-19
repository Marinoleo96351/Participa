package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.aula.AulaService;
import br.edu.utfpr.participa.duvida.dto.ApoioDTO;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ApoioService {

    private final ApoioRepository apoioRepository;
    private final DuvidaRepository duvidaRepository;
    private final AulaService aulaService;

    public ApoioService(ApoioRepository apoioRepository, DuvidaRepository duvidaRepository, AulaService aulaService) {
        this.apoioRepository = apoioRepository;
        this.duvidaRepository = duvidaRepository;
        this.aulaService = aulaService;
    }

    @Transactional
    public ApoioDTO apoiar(Long duvidaId, Usuario estudante) {
        Duvida duvida = buscarEVerificarAcesso(duvidaId, estudante);
        if (duvida.getAutor().getId().equals(estudante.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível apoiar a própria dúvida.");
        }
        if (!apoioRepository.existsById_DuvidaIdAndId_UsuarioId(duvidaId, estudante.getId())) {
            apoioRepository.save(new Apoio(duvida, estudante));
        }
        return estadoAtual(duvidaId, estudante.getId());
    }

    @Transactional
    public ApoioDTO desapoiar(Long duvidaId, Usuario estudante) {
        buscarEVerificarAcesso(duvidaId, estudante);
        apoioRepository.findById_DuvidaIdAndId_UsuarioId(duvidaId, estudante.getId())
                .ifPresent(apoioRepository::delete);
        return estadoAtual(duvidaId, estudante.getId());
    }

    public Map<Long, Integer> contarPorDuvida(List<Long> duvidaIds) {
        if (duvidaIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> totais = new HashMap<>();
        for (ApoioRepository.ContagemApoio contagem : apoioRepository.contarPorDuvida(duvidaIds)) {
            totais.put(contagem.getDuvidaId(), (int) contagem.getTotal());
        }
        return totais;
    }

    public Set<Long> apoiadasPor(Usuario usuario, List<Long> duvidaIds) {
        if (duvidaIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(apoioRepository.buscarApoiadasPorUsuario(usuario.getId(), duvidaIds));
    }

    private Duvida buscarEVerificarAcesso(Long duvidaId, Usuario usuario) {
        Duvida duvida = duvidaRepository.findById(duvidaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dúvida não encontrada."));
        aulaService.buscarEVerificarAcesso(duvida.getAula().getId(), usuario);
        return duvida;
    }

    private ApoioDTO estadoAtual(Long duvidaId, Long usuarioId) {
        int total = (int) apoioRepository.countById_DuvidaId(duvidaId);
        boolean apoiada = apoioRepository.existsById_DuvidaIdAndId_UsuarioId(duvidaId, usuarioId);
        return new ApoioDTO(total, apoiada);
    }
}
