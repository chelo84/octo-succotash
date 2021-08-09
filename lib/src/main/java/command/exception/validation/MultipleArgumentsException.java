package command.exception.validation;

import spi.CommandService;

import java.lang.reflect.Method;

public class MultipleArgumentsException extends CommandValidationException {
    public MultipleArgumentsException(Class<? extends CommandService> clazz, Method method, Object... arguments) {
        super("The command's \"%s\" method must have only one argument.", clazz, method, arguments);
    }
}
