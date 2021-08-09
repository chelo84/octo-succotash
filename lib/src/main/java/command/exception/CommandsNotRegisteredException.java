package command.exception;

public class CommandsNotRegisteredException extends RuntimeException {
    public CommandsNotRegisteredException() {
        super("Commands not registered yet, try calling getInstance after registerCommands()");
    }
}
