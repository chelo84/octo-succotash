package command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spi.CommandService;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static command.annotation.CommandArg.ArgType.UNDEFINED;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@With
@Value
@Slf4j
@Builder
@ToString
@RequiredArgsConstructor
public class Cmd {

    String command;
    Class<? extends CommandService> service;
    Method method;
    String description;
    String example;

    public static Cmd create(CommandService service, Method method) {
        command.annotation.Command commandAnn = method.getAnnotation(command.annotation.Command.class);
        if (commandAnn == null)
            return null;
        else if (commandAnn.value().equals(""))
            throw new CommandValidationException("Command does not have a value", service.getClass(), method);

        final String commandValue = commandAnn.value().trim();
        final String commandDescription = isNotBlank(commandAnn.description()) ? commandAnn.description() : null;
        final String commandExample = createExample(commandAnn);

        validateCommand(service, method, commandValue);

        return Cmd.builder()
                .command(commandValue)
                .service(service.getClass())
                .method(method)
                .description(commandDescription)
                .example(isNotBlank(commandExample) ? commandExample : null)
                .build();
    }

    private static void validateCommand(CommandService service, Method method, String commandValue) {
        if (method.getGenericParameterTypes()[0] != MessageCreateEvent.class) {
            throw new WrongParameterTypeException(service.getClass(), method, commandValue, method.getGenericParameterTypes()[0].getTypeName());
        } else if (!isSecondArgAFluxOfString(method)) {
            throw new WrongParameterTypeException(service.getClass(), method, commandValue, method.getGenericParameterTypes()[1].getTypeName());
        } else if (method.getReturnType() != Mono.class) {
            throw new WrongReturnTypeException(service.getClass(), method, commandValue);
        }
    }

    /**
     * Checks whether the second argument of the method is equal to Flux&lt;String&gt;
     */
    private static boolean isSecondArgAFluxOfString(Method method) {
        Optional<Type> rawType = Optional.empty();
        Optional<Type> genericType = Optional.empty();
        if (method.getGenericParameterTypes().length > 1) {
            ParameterizedType genericParameterType = (ParameterizedType) method.getGenericParameterTypes()[1];
            rawType = Optional.of(genericParameterType.getRawType());
            genericType = Optional.of(genericParameterType.getActualTypeArguments())
                    .filter(it -> it.length > 0)
                    .map(it -> it[0]);
        }

        return rawType.isEmpty() || rawType.get() == Flux.class &&
                genericType.isEmpty() || genericType.get() == String.class;
    }

    /**
     * Create an example of the command like "<i>(PREFIX)</i><strong>help</strong> &lt;<strong>command</strong>:<strong>text</strong>&gt;"
     *
     * @param commandAnn The command annotation
     * @return String containing the command's example
     */
    private static String createExample(command.annotation.Command commandAnn) {
        StringBuilder sb = new StringBuilder(format("{0}{1}", Commands.COMMAND_PREFIX, commandAnn.value()));

        Arrays.stream(commandAnn.args())
                .filter(Objects::nonNull)
                .forEach(it -> sb.append(" ")
                        .append(it.required() ? "<" : "(")
                        .append(it.value())
                        .append(!it.type().equals(UNDEFINED) ? format(":{0}", it.type().getName()) : "")
                        .append(it.required() ? ">" : ")")
                );
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cmd command1 = (Cmd) o;
        return getCommand().equals(command1.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommand());
    }
}
