package command.exception.validation;

import command.Command;

public class CommandAlreadyBoundException extends CommandValidationException {
    public CommandAlreadyBoundException(Command command) {
        super("The command \"%s\" has multiple bindings.", command.getCommand());
    }
}
