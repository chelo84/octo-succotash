package command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import spi.CommandService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static command.annotation.CommandArg.ArgType.UNDEFINED;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;

@With
@Value
@Slf4j
@Builder
@RequiredArgsConstructor
public class Command {

    String command;
    Class<? extends CommandService> service;
    Method method;
    String description;
    String example;

    public static Command create(CommandService service, Method method) {
        command.annotation.Command commandAnn = method.getAnnotation(command.annotation.Command.class);
        if (isNull(commandAnn))
            return null;
        else if (isNull(commandAnn.value()))
            throw new CommandValidationException("Command does not have a value", service.getClass(), method);

        final String commandValue = commandAnn.value().trim();

        /*if (method.getParameterTypes().length > 1) {
            throw new MultipleArgumentsException(service.getClass(), method, commandValue);
        } else*/
        if (method.getParameterTypes()[0] != MessageCreateEvent.class) {
            throw new WrongParameterTypeException(service.getClass(), method, commandValue);
        } else if (method.getReturnType() != Mono.class) {
            throw new WrongReturnTypeException(service.getClass(), method, commandValue);
        }

        log.info(createExample(commandAnn));
        return Command.builder()
                .command(commandValue)
                .service(service.getClass())
                .method(method)
                .description(commandAnn.description())
                .example(createExample(commandAnn))
                .build();
    }

    private static String createExample(command.annotation.Command commandAnn) {
        StringBuilder sb = new StringBuilder(format("{0}{1}", Commands.COMMAND_PREFIX, commandAnn.value()));

        Arrays.stream(commandAnn.args())
                .filter(Objects::nonNull)
                .forEach(it -> sb.append(" {")
                        .append(it.value())
                        .append(!it.type().equals(UNDEFINED) ? format(":{0}", it.type().getName()) : "")
                        .append("}")
                );
        return sb.toString();
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
