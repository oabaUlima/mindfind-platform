package pe.edu.ulima.mindfind.triage_matching.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.CrisisIndicatorDetectedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.MatchingRankingGeneratedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.TriageEvaluationCompletedEvent;

/**
 * Policy interna del contexto {@code triage_matching} que registra trazabilidad clínica
 * tras confirmar la persistencia de eventos de crisis, finalización y ranking Top 5.
 */
@Component
public class TriageDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TriageDomainEventHandler.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CrisisIndicatorDetectedEvent event) {
        log.warn("[TriageMatching] ALERTA DE CRISIS confirmada (evaluacionId={}, nivelRiesgo={}, palabra='{}', bloqueo={})",
                event.evaluacionId(), event.nivelRiesgo(), event.palabraDetectada(), event.emparejamientoBloqueado());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TriageEvaluationCompletedEvent event) {
        log.info("[TriageMatching] Evaluación de triage completada (evaluacionId={}, nivelRiesgo={}, modalidad={})",
                event.evaluacionId(), event.nivelRiesgo(), event.modalidadPreferida());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MatchingRankingGeneratedEvent event) {
        log.info("[TriageMatching] Ranking Top 5 generado (evaluacionId={}, resultados={}, mejorCompatibilidad={}%)",
                event.evaluacionId(), event.totalResultados(), event.mejorPorcentajeCompatibilidad());
    }
}
