package pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.NumeroColegiatura;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.TarifaSesion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.repositories.PsicologoRepository;
import pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.entities.PsicologoJpaEntity;
import pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.repositories.PsicologoJpaRepository;
import pe.edu.ulima.mindfind.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de repositorio que traduce entre el agregado {@link Psicologo}
 * y la entidad JPA {@link PsicologoJpaEntity}, publicando los eventos de dominio al guardar.
 */
@Repository
public class PsicologoRepositoryAdapter implements PsicologoRepository {

    private final PsicologoJpaRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public PsicologoRepositoryAdapter(PsicologoJpaRepository jpaRepository, DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Psicologo save(Psicologo psicologo) {
        var entity = psicologo.getId() == null
                ? new PsicologoJpaEntity()
                : jpaRepository.findById(psicologo.getId()).orElseThrow();
        toEntity(psicologo, entity);
        var saved = jpaRepository.save(entity);
        eventPublisher.publishAll(psicologo);
        return toDomain(saved);
    }

    @Override
    public Optional<Psicologo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Psicologo> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Psicologo> findByEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        return jpaRepository.findByEstadoVerificacion(estadoVerificacion).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByCorreoIgnoreCase(String correo) {
        return jpaRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existsByNumeroColegiatura(String numeroColegiatura) {
        return jpaRepository.existsByNumeroColegiatura(numeroColegiatura);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    private void toEntity(Psicologo psicologo, PsicologoJpaEntity entity) {
        entity.setNombreCompleto(psicologo.getNombreCompleto());
        entity.setCorreo(psicologo.getCorreo());
        entity.setTelefono(psicologo.getTelefono());
        entity.setNumeroColegiatura(psicologo.getNumeroColegiatura().value());
        entity.setEnfoqueTeorico(psicologo.getEnfoqueTeorico());
        entity.getEspecialidades().clear();
        entity.getEspecialidades().addAll(psicologo.getEspecialidades());
        entity.getModalidadesAtencion().clear();
        entity.getModalidadesAtencion().addAll(psicologo.getModalidadesAtencion());
        entity.setTarifaMonto(psicologo.getTarifaPorSesion().monto());
        entity.setTarifaMoneda(psicologo.getTarifaPorSesion().moneda());
        entity.setDisponibilidadHoraria(psicologo.getDisponibilidadHoraria());
        entity.setEstadoVerificacion(psicologo.getEstadoVerificacion());
    }

    private Psicologo toDomain(PsicologoJpaEntity entity) {
        return Psicologo.restore(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getCorreo(),
                entity.getTelefono(),
                new NumeroColegiatura(entity.getNumeroColegiatura()),
                entity.getEnfoqueTeorico(),
                entity.getEspecialidades(),
                entity.getModalidadesAtencion(),
                new TarifaSesion(entity.getTarifaMonto(), entity.getTarifaMoneda()),
                entity.getDisponibilidadHoraria(),
                entity.getEstadoVerificacion());
    }
}
