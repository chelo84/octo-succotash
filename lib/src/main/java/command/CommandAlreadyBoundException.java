package command;

public class CommandAlreadyBoundException extends CommandValidationException {
    public CommandAlreadyBoundException(Cmd command) {
        super("The command \"%s\" has multiple bindings.", command.getCommand());
    }
}
