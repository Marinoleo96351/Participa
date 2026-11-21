package br.edu.utfpr.participa.duvida;

import br.edu.utfpr.participa.aula.Aula;
import br.edu.utfpr.participa.aula.AulaService;
import br.edu.utfpr.participa.duvida.dto.DuvidaDTO;
import br.edu.utfpr.participa.usuario.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DuvidaService {

    private static final int TAMANHO_MAXIMO_TEXTO = 500;
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_PRAZO = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");

    private final DuvidaRepository duvidaRepository;
    private final DuvidaMapper duvidaMapper;
    private final AulaService aulaService;
    private final ApoioService apoioService;

    public DuvidaService(DuvidaRepository duvidaRepository, DuvidaMapper duvidaMapper, AulaService aulaService,
                          ApoioService apoioService) {
        this.duvidaRepository = duvidaRepository;
        this.duvidaMapper = duvidaMapper;
        this.aulaService = aulaService;
        this.apoioService = apoioService;
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
        boolean posAula = verificarSituacaoEDeterminarPosAula(aula);
        Duvida duvida = new Duvida(aula, estudante, anonima, texto, StatusDuvida.PUBLICADA, posAula);
        return duvidaRepository.save(duvida);
    }

    private boolean verificarSituacaoEDeterminarPosAula(Aula aula) {
        return switch (aula.getSituacao()) {
            case ABERTA -> false;
            case POS_AULA -> {
                if (!Instant.now().isBefore(aula.getPrazoPosAula())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "O envio de dúvidas desta aula terminou em " + formatarPrazo(aula.getPrazoPosAula()) + ".");
                }
                yield true;
            }
            case ENCERRADA -> throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O envio de dúvidas para esta aula está encerrado.");
        };
    }

    private String formatarPrazo(Instant prazo) {
        return FORMATO_PRAZO.format(prazo.atZone(FUSO_PADRAO));
    }

    public List<DuvidaDTO> listarVisiveis(Aula aula, Usuario usuarioLogado) {
        List<Duvida> visiveis = duvidaRepository.findByAulaId(aula.getId()).stream()
                .filter(duvida -> visivelPara(duvida, usuarioLogado))
                .toList();
        List<Long> ids = visiveis.stream().map(Duvida::getId).toList();
        Map<Long, Integer> totais = apoioService.contarPorDuvida(ids);
        Set<Long> apoiadas = apoioService.apoiadasPor(usuarioLogado, ids);
        Comparator<Duvida> ordenacao = Comparator.comparing(Duvida::isRespondida)
                .thenComparing(Comparator.comparing((Duvida d) -> totais.getOrDefault(d.getId(), 0)).reversed())
                .thenComparing(Duvida::getCriadaEm);
        return visiveis.stream()
                .sorted(ordenacao)
                .map(duvida -> duvidaMapper.toDTO(duvida, usuarioLogado,
                        totais.getOrDefault(duvida.getId(), 0), apoiadas.contains(duvida.getId())))
                .toList();
    }

    public DuvidaDTO marcarRespondida(Long duvidaId, boolean respondida, Usuario professor) {
        Duvida duvida = duvidaRepository.findById(duvidaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dúvida não encontrada."));
        if (!duvida.getAula().getTurma().getProfessor().getId().equals(professor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta dúvida.");
        }
        duvida.marcarRespondida(respondida);
        duvidaRepository.save(duvida);
        int apoios = apoioService.contarPorDuvida(List.of(duvidaId)).getOrDefault(duvidaId, 0);
        return duvidaMapper.toDTO(duvida, professor, apoios, false);
    }

    private boolean visivelPara(Duvida duvida, Usuario usuario) {
        return switch (duvida.getStatus()) {
            case PUBLICADA -> true;
            case PENDENTE -> duvida.getAutor().getId().equals(usuario.getId());
            case OCULTA -> false;
        };
    }
}
