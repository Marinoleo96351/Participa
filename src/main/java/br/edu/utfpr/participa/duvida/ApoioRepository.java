package br.edu.utfpr.participa.duvida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApoioRepository extends JpaRepository<Apoio, ApoioId> {

    boolean existsById_DuvidaIdAndId_UsuarioId(Long duvidaId, Long usuarioId);

    Optional<Apoio> findById_DuvidaIdAndId_UsuarioId(Long duvidaId, Long usuarioId);

    long countById_DuvidaId(Long duvidaId);

    @Query("select a.id.duvidaId as duvidaId, count(a) as total "
            + "from Apoio a where a.id.duvidaId in :duvidaIds group by a.id.duvidaId")
    List<ContagemApoio> contarPorDuvida(@Param("duvidaIds") List<Long> duvidaIds);

    @Query("select a.id.duvidaId from Apoio a where a.id.usuarioId = :usuarioId and a.id.duvidaId in :duvidaIds")
    List<Long> buscarApoiadasPorUsuario(@Param("usuarioId") Long usuarioId, @Param("duvidaIds") List<Long> duvidaIds);

    interface ContagemApoio {

        Long getDuvidaId();

        long getTotal();
    }
}
