package pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.ulima.mindfind.shared.infrastructure.events.DomainEventPublisher;
import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PorcentajeCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PresupuestoMaximo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ProtocoloCrisis;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.RecursoEmergencia;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.repositories.EvaluacionTriageRepository;
import pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.entities.EvaluacionTriageJpaEntity;
import pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.entities.ResultadoCompatibilidadJpaEntity;
import pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.repositories.EvaluacionTriageJpaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de repositorio que traduce entre {@link EvaluacionTriage} y {@link EvaluacionTriageJpaEntity},
 * publicando los eventos de dominio tras confirmar el guardado.
 */
@Repository
public class EvaluacionTriageRepositoryAdapter implements EvaluacionTriageRepository {

    private final EvaluacionTriageJpaRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    public EvaluacionTriageRepositoryAdapter(
            EvaluacionTriageJpaRepository jpaRepository,
            DomainEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EvaluacionTriage save(EvaluacionTriage evaluacion) {
        var entity = evaluacion.getId() == null
                ? new EvaluacionTriageJpaEntity()
                : jpaRepository.findById(evaluacion.getId()).orElseThrow();
        toEntity(evaluacion, entity);
        var saved = jpaRepository.save(entity);
        eventPublisher.publishAll(evaluacion);
        return toDomain(saved);
    }

    @Override
    public Optional<EvaluacionTriage> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<EvaluacionTriage> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private void toEntity(EvaluacionTriage domain, EvaluacionTriageJpaEntity entity) {
        entity.setNombrePaciente(domain.getNombrePaciente());
        entity.setEdadPaciente(domain.getEdadPaciente());
        entity.setMotivoConsulta(domain.getMotivoConsulta());
        entity.getSintomasDeclarados().clear();
        entity.getSintomasDeclarados().addAll(domain.getSintomasDeclarados());
        entity.setModalidadPreferida(domain.getModalidadPreferida());
        entity.setPresupuestoMonto(domain.getPresupuestoMaximo().monto());
        entity.setPresupuestoMoneda(domain.getPresupuestoMaximo().moneda());
        entity.setDisponibilidadPreferida(domain.getDisponibilidadPreferida());
        entity.setEnfoquePreferido(domain.getEnfoquePreferido());
        entity.setPreferenciasProfesional(domain.getPreferenciasProfesional());
        entity.setConsentimientoCifrado(domain.isConsentimientoCifrado());
        entity.setEstado(domain.getEstado());
        entity.setNivelRiesgo(domain.getNivelRiesgo());
        entity.setFechaInicio(domain.getFechaInicio());
        entity.setFechaFinalizacion(domain.getFechaFinalizacion());

        var protocolo = domain.getProtocoloCrisis();
        if (protocolo != null) {
            entity.setCrisisActiva(true);
            entity.setCrisisPalabraDetectada(protocolo.palabraDetectada());
            entity.setCrisisNivelRiesgo(protocolo.nivelRiesgo());
            entity.setCrisisEmparejamientoBloqueado(protocolo.emparejamientoBloqueado());
            entity.setCrisisAlertaCerrada(protocolo.alertaCerrada());
        } else {
            entity.setCrisisActiva(false);
            entity.setCrisisPalabraDetectada(null);
            entity.setCrisisNivelRiesgo(null);
            entity.setCrisisEmparejamientoBloqueado(false);
            entity.setCrisisAlertaCerrada(false);
        }

        entity.getResultadosCompatibilidad().clear();
        for (var resultado : domain.getResultadosCompatibilidad()) {
            var child = new ResultadoCompatibilidadJpaEntity();
            child.setEvaluacion(entity);
            child.setIdPsicologo(resultado.idPsicologo());
            child.setNombrePsicologo(resultado.nombrePsicologo());
            child.setEnfoqueTeorico(resultado.enfoqueTeorico());
            child.getEspecialidades().addAll(resultado.especialidades());
            child.getModalidadesAtencion().addAll(resultado.modalidadesAtencion());
            child.setTarifaPorSesion(resultado.tarifaPorSesion());
            child.setDisponibilidadHoraria(resultado.disponibilidadHoraria());
            child.setPorcentajeCompatibilidad(resultado.porcentajeCompatibilidad().valor());
            child.setPosicionRanking(resultado.posicionRanking());
            child.setEsMatchParcial(resultado.esMatchParcial());
            child.getRazonesCompatibilidad().addAll(resultado.razonesCompatibilidad());
            entity.getResultadosCompatibilidad().add(child);
        }
    }

    private EvaluacionTriage toDomain(EvaluacionTriageJpaEntity entity) {
        ProtocoloCrisis protocolo = null;
        if (entity.isCrisisActiva()) {
            protocolo = new ProtocoloCrisis(
                    entity.getCrisisPalabraDetectada(),
                    entity.getCrisisNivelRiesgo(),
                    entity.isCrisisEmparejamientoBloqueado(),
                    entity.isCrisisAlertaCerrada(),
                    RecursoEmergencia.recursosPeruPorDefecto());
        }

        var resultados = entity.getResultadosCompatibilidad().stream()
                .sorted(Comparator.comparingInt(ResultadoCompatibilidadJpaEntity::getPosicionRanking))
                .map(child -> new ResultadoCompatibilidad(
                        child.getIdPsicologo(),
                        child.getNombrePsicologo(),
                        child.getEnfoqueTeorico(),
                        child.getEspecialidades(),
                        child.getModalidadesAtencion().stream()
                                .sorted(Comparator.comparing(Enum::name))
                                .toList(),
                        child.getTarifaPorSesion(),
                        child.getDisponibilidadHoraria(),
                        new PorcentajeCompatibilidad(child.getPorcentajeCompatibilidad()),
                        child.getPosicionRanking(),
                        child.isEsMatchParcial(),
                        child.getRazonesCompatibilidad()))
                .toList();

        return EvaluacionTriage.restore(
                entity.getId(),
                entity.getNombrePaciente(),
                entity.getEdadPaciente(),
                entity.getMotivoConsulta(),
                entity.getSintomasDeclarados(),
                entity.getModalidadPreferida(),
                new PresupuestoMaximo(entity.getPresupuestoMonto(), entity.getPresupuestoMoneda()),
                entity.getDisponibilidadPreferida(),
                entity.getEnfoquePreferido(),
                entity.getPreferenciasProfesional(),
                entity.isConsentimientoCifrado(),
                entity.getEstado(),
                entity.getNivelRiesgo(),
                entity.getFechaInicio(),
                entity.getFechaFinalizacion(),
                protocolo,
                resultados);
    }
}
