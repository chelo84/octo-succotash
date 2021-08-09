package command.exception.validation;

import service.CommandService;

import java.lang.reflect.Method;

public class WrongReturnTypeException extends CommandValidationException {
    public WrongReturnTypeException(Class<? extends CommandService> clazz, Method method, Object... arguments) {
        super("The command's \"%s\" return type is not Mono.", clazz, method, arguments);
    }
}
