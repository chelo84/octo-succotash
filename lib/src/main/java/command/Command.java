package command;

import command.exception.validation.MultipleArgumentsException;
import command.exception.validation.WrongParameterTypeException;
import command.exception.validation.WrongReturnTypeException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import reactor.core.publisher.Mono;
import spi.CommandService;

import java.lang.reflect.Method;
import java.util.Objects;

@With
@Value
@Builder
@RequiredArgsConstructor
public class Command {

    String command;
    Class<? extends CommandService> service;
    Method method;

    public static Command create(CommandService service, Method method) {
        command.annotation.Command command = method.getAnnotation(command.annotation.Command.class);
        if (command == null)
            return null;

        final String commandValue = command.value().trim();
        final Class<? extends CommandService> serviceClass = service.getClass();

        if (method.getParameterTypes().length > 1) {
            throw new MultipleArgumentsException(serviceClass, method, commandValue);
        } else if (method.getParameterTypes()[0] != MessageCreateEvent.class) {
            throw new WrongParameterTypeException(serviceClass, method, commandValue);
        } else if (method.getReturnType() != Mono.class) {
            throw new WrongReturnTypeException(serviceClass, method, commandValue);
        }

        return Command.builder()
                .command(commandValue)
                .service(serviceClass)
                .method(method)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command1 = (Command) o;
        return getCommand().equals(command1.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommand());
    }
}
