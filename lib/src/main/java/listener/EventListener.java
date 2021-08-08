package listener;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;
import service.Service;

public interface EventListener<T extends Event> extends Service {
    Mono<?> execute(T event);
}
