package pe.edu.ulima.mindfind.shared.infrastructure.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.edu.ulima.mindfind.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Publishes the events raised by an aggregate. Called by the repository adapters
 * right after the aggregate is saved.
 * Here events travel in memory (Spring). In production this is where a broker or an outbox would plug in.
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishAll(AbstractDomainAggregateRoot aggregate) {
        aggregate.pullDomainEvents().forEach(event -> {
            log.info("[Domain event] {} raised by {} -> {}",
                    event.getClass().getSimpleName(), aggregate.getClass().getSimpleName(), event);
            applicationEventPublisher.publishEvent(event);
        });
    }
}
