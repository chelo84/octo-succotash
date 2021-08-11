package command;

import spi.CommandService;

import java.lang.reflect.Method;

public class CommandValidationException extends RuntimeException {
    public CommandValidationException(String msg, Class<? extends CommandService> clazz, Method method) {
        super(msg.trim() + " (Class: \"" + clazz.getSimpleName() + "\", Method: \"" + method.getName() + "\")");
    }

    public CommandValidationException(String msg, Class<? extends CommandService> clazz, Method method, Object... arguments) {
        super(String.format(msg.trim() + " (Class: \"" + clazz.getSimpleName() + "\", Method: \"" + method.getName() + "\")", arguments));
    }

    public CommandValidationException(String msg, Object... arguments) {
        super(String.format(msg.trim(), arguments));
    }
}
