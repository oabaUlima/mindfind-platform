package pe.edu.ulima.mindfind.triage_matching.domain.model.events;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;

import java.time.Instant;

/**
 * Domain Event emitido cuando el cuestionario de triage finaliza sin crisis (HU-04).
 */
public record TriageEvaluationCompletedEvent(
        Long evaluacionId,
        NivelRiesgo nivelRiesgo,
        ModalidadPreferida modalidadPreferida,
        Instant occurredAt) {
}
