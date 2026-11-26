package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistVerifiedEvent;

/**
 * Traduce el evento interno {@link PsychologistVerifiedEvent} en el evento de integración
 * {@link PsychologistVerifiedIntegrationEvent} para consumo de otros Bounded Contexts.
 */
@Component
public class PsychologistVerifiedIntegrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PsychologistVerifiedIntegrationEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public PsychologistVerifiedIntegrationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    public void on(PsychologistVerifiedEvent event) {
        log.info("[Identidad] Traduciendo PsychologistVerifiedEvent (psicologoId={}) a PsychologistVerifiedIntegrationEvent",
                event.psicologoId());
        applicationEventPublisher.publishEvent(
                new PsychologistVerifiedIntegrationEvent(event.psicologoId(), event.occurredAt()));
    }
}
