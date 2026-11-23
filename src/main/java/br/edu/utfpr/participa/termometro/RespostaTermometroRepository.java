package br.edu.utfpr.participa.termometro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RespostaTermometroRepository extends JpaRepository<RespostaTermometro, RespostaTermometroId> {

    boolean existsById_TermometroIdAndId_UsuarioId(Long termometroId, Long usuarioId);

    Optional<RespostaTermometro> findById_TermometroIdAndId_UsuarioId(Long termometroId, Long usuarioId);

    // Só somas por opção: nunca devolve quem respondeu.
    @Query("select r.opcao as opcao, count(r) as total "
            + "from RespostaTermometro r where r.id.termometroId = :termometroId group by r.opcao")
    java.util.List<ContagemOpcao> contarPorOpcao(@Param("termometroId") Long termometroId);

    interface ContagemOpcao {

        OpcaoResposta getOpcao();

        long getTotal();
    }
}