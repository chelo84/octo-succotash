package service;

import listener.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public final class EventListeners {

    public static <T extends EventListener<?>> T getListener(Class<T> clazz) {
        log.info("getListener ({})", clazz.getSimpleName());
        return (T) Services.getAll(EventListener.class)
                .stream()
                .filter(it -> it.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    public static <T extends EventListener<?>> List<T> getAll(Class<T> clazz) {
        log.info("getAll ({})", clazz.getSimpleName());
        return Services.getAll(EventListener.class)
                .stream()
                .filter(it -> it.getClass().equals(clazz) || Arrays.asList(it.getClass().getInterfaces()).contains(clazz))
                .map(it -> (T) it)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("rawtypes")
    public static List<EventListener> getAll() {
        log.info("getAll");
        return Services.getAll(EventListener.class);
    }
}
