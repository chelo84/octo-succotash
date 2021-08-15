package command;

import command.message.MessageCommand;

public class CommandAlreadyBoundException extends CommandValidationException {
    public CommandAlreadyBoundException(MessageCommand messageCommand) {
        super("The command \"%s\" has multiple bindings.", messageCommand.getCommand());
    }
}
