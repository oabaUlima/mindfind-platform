package pe.edu.ulima.mindfind.identidad_acreditacion.domain.repositories;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio del agregado {@link Psicologo}.
 * Libre de JPA y Spring; su adaptador vive en la capa de infraestructura.
 */
public interface PsicologoRepository {

    Psicologo save(Psicologo psicologo);

    Optional<Psicologo> findById(Long id);

    List<Psicologo> findAll();

    List<Psicologo> findByEstadoVerificacion(EstadoVerificacion estadoVerificacion);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNumeroColegiatura(String numeroColegiatura);

    long count();
}
