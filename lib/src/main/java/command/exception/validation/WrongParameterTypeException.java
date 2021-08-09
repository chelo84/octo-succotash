package command.exception.validation;

import service.CommandService;

import java.lang.reflect.Method;

public class WrongParameterTypeException extends CommandValidationException {
    public WrongParameterTypeException(Class<? extends CommandService> clazz, Method method, Object... arguments) {
        super("The command's \"%s\" method argument must be of type MessageCreateEvent.", clazz, method, arguments);
    }
}
