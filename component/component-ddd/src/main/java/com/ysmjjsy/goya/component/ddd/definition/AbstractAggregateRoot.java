package com.ysmjjsy.goya.component.ddd.definition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 22:39
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class AbstractAggregateRoot<T extends Serializable> extends AbstractEntity<T> {

    @Serial
    private static final long serialVersionUID = 4144349344032535663L;

    private final List<DomainEvent> domainEvents = new LinkedList<>();

    /**
     * publish domain event
     * <p>
     * AggregateRoot record event
     * </p>
     *
     * @param event event
     */
    protected void publishEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * get domain event
     *
     * @return event
     */
    public List<DomainEvent> getDomainEvents() {
        return new LinkedList<>(domainEvents);
    }

    /**
     * clear domain event
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
