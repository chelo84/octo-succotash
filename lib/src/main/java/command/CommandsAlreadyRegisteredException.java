package command;

public class CommandsAlreadyRegisteredException extends RuntimeException {
    public CommandsAlreadyRegisteredException() {
        super("Commands instance is already registered!");
    }
}
