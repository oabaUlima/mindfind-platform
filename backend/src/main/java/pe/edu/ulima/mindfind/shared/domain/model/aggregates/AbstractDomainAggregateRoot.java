package pe.edu.ulima.mindfind.shared.domain.model.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for every aggregate root. Pure Java: no JPA, no Spring.
 * It only knows how to remember the domain events the aggregate raised.
 */
public abstract class AbstractDomainAggregateRoot {

    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerDomainEvent(Object event) {
        domainEvents.add(Objects.requireNonNull(event));
    }

    /** Returns the raised events and forgets them, so they are published only once. */
    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
