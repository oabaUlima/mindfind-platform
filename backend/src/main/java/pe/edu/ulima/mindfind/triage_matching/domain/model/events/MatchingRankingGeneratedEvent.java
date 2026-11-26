package pe.edu.ulima.mindfind.triage_matching.domain.model.events;

import java.time.Instant;

/**
 * Domain Event emitido cuando el motor genera y guarda el ranking Top 5 de compatibilidad (HU-05).
 */
public record MatchingRankingGeneratedEvent(
        Long evaluacionId,
        int totalResultados,
        double mejorPorcentajeCompatibilidad,
        Instant occurredAt) {
}
