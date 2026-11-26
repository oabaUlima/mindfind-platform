package pe.edu.ulima.mindfind.triage_matching.domain.model.events;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;

import java.time.Instant;

/**
 * Domain Event emitido cuando {@code EvaluacionTriage} detecta un indicador de riesgo crítico (HU-06).
 */
public record CrisisIndicatorDetectedEvent(
        Long evaluacionId,
        String palabraDetectada,
        NivelRiesgo nivelRiesgo,
        boolean emparejamientoBloqueado,
        Instant occurredAt) {
}
