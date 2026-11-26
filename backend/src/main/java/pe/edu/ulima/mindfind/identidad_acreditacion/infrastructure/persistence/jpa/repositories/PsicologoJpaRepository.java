package pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.entities.PsicologoJpaEntity;

import java.util.List;

/** Repositorio Spring Data JPA sobre la entidad de persistencia. Solo el adaptador lo utiliza. */
public interface PsicologoJpaRepository extends JpaRepository<PsicologoJpaEntity, Long> {

    List<PsicologoJpaEntity> findByEstadoVerificacion(EstadoVerificacion estadoVerificacion);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNumeroColegiatura(String numeroColegiatura);
}
