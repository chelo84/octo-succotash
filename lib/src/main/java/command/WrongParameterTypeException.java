package command;

import spi.CommandService;

import java.lang.reflect.Method;

public class WrongParameterTypeException extends CommandValidationException {
    public WrongParameterTypeException(Class<? extends CommandService> clazz,
                                       Method method,
                                       Object... arguments) {
        super("Command: %s. The method argument must be of types (MessageCreateEvent, Flux<String>). Actual parameter: %s.", clazz, method, arguments);
    }
}
