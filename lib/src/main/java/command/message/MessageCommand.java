package command.message;

import command.CommandValidationException;
import command.WrongParameterTypeException;
import command.WrongReturnTypeException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import event.annotation.OnMessage;
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

import static event.annotation.MessageArgument.ArgType.UNDEFINED;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@With
@Value
@Slf4j
@Builder
@ToString
@RequiredArgsConstructor
public class MessageCommand {

    String command;
    Class<? extends CommandService> service;
    Method method;
    String description;
    String example;
    Class<?>[] parameterTypes;

    public static MessageCommand create(CommandService service, Method method) {
        OnMessage onMessage = method.getAnnotation(OnMessage.class);
        if (onMessage == null)
            return null;
        else if (onMessage.value().equals(""))
            throw new CommandValidationException("Command does not have a value", service.getClass(), method);

        final String commandValue = onMessage.value().trim();
        final String commandDescription = isNotBlank(onMessage.description()) ? onMessage.description() : null;
        final String commandExample = createExample(onMessage);
        final Class<?>[] parameterTypes = method.getParameterTypes();

        validateCommand(service, method, commandValue);

        return MessageCommand.builder()
                .command(commandValue)
                .service(service.getClass())
                .method(method)
                .description(commandDescription)
                .example(isNotBlank(commandExample) ? commandExample : null)
                .parameterTypes(parameterTypes)
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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
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
     * @param onMessageAnn The command annotation
     * @return String containing the command's example
     */
    private static String createExample(OnMessage onMessageAnn) {
        StringBuilder sb = new StringBuilder(format("{0}{1}", MessageCommands.COMMAND_PREFIX, onMessageAnn.value()));

        Arrays.stream(onMessageAnn.args())
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
        MessageCommand messageCommand1 = (MessageCommand) o;
        return getCommand().equals(messageCommand1.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommand());
    }
}
