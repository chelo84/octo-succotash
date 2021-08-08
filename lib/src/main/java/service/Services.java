package service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;

@Slf4j
public final class Services {
    @SuppressWarnings("rawtypes")
    private final static Map<Class<?>, ServiceLoader> serviceLoaderByClass = new HashMap<>();

    public static <T extends Service> T getService(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = getOrLoadServiceLoader(clazz);
        for (T service : serviceLoader) {
            if (service != null) {
                return service;
            }
        }

        throw new RuntimeException(format("Service {0} not found", clazz.getName()));
    }

    public static <T extends Service> List<T> getAll(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = getOrLoadServiceLoader(clazz);

        return serviceLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked rawtypes")
    private static <T extends Service> ServiceLoader<T> getOrLoadServiceLoader(Class<T> clazz) {
        ServiceLoader serviceLoader = serviceLoaderByClass.get(clazz);
        if (isNull(serviceLoader)) {
            serviceLoader = loadServiceLoader(clazz);
        }

        return (ServiceLoader<T>) serviceLoader;
    }

    @SneakyThrows
    @SuppressWarnings("rawtypes")
    private static <T extends Service> ServiceLoader loadServiceLoader(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        serviceLoaderByClass.put(clazz, loader);

        return loader;
    }
}