package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.aula.AulaService;
import br.edu.utfpr.participa.aula.SituacaoAula;
import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DuvidaService {

    private static final int TAMANHO_MAXIMO_TEXTO = 500;

    private final DuvidaRepository duvidaRepository;
    private final DuvidaMapper duvidaMapper;
    private final AulaService aulaService;

    public DuvidaService(DuvidaRepository duvidaRepository, DuvidaMapper duvidaMapper, AulaService aulaService) {
        this.duvidaRepository = duvidaRepository;
        this.duvidaMapper = duvidaMapper;
        this.aulaService = aulaService;
    }

    public Duvida enviar(Long aulaId, String textoBruto, boolean anonima, Usuario estudante) {
        Aula aula = aulaService.buscarEVerificarAcesso(aulaId, estudante);
        String texto = textoBruto == null ? "" : textoBruto.trim();
        if (texto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O texto da dúvida não pode ficar vazio.");
        }
        if (texto.length() > TAMANHO_MAXIMO_TEXTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O texto da dúvida pode ter até 500 caracteres.");
        }
        if (aula.getSituacao() != SituacaoAula.ABERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O envio de dúvidas para esta aula está encerrado.");
        }
        Duvida duvida = new Duvida(aula, estudante, anonima, texto, StatusDuvida.PUBLICADA, false);
        return duvidaRepository.save(duvida);
    }

    public List<DuvidaDTO> listarVisiveis(Aula aula, Usuario usuarioLogado) {
        return duvidaRepository.findByAulaIdOrderByRespondidaAscCriadaEmAsc(aula.getId()).stream()
                .filter(duvida -> visivelPara(duvida, usuarioLogado))
                .map(duvida -> duvidaMapper.toDTO(duvida, usuarioLogado))
                .toList();
    }

    private boolean visivelPara(Duvida duvida, Usuario usuario) {
        return switch (duvida.getStatus()) {
            case PUBLICADA -> true;
            case PENDENTE -> duvida.getAutor().getId().equals(usuario.getId());
            case OCULTA -> false;
        };
    }
}
