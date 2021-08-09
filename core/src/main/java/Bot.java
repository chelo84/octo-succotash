import service.CommandService;
import service.CommandService.CommandAndMethod;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import command.Command;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import service.Services;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class Bot {

    private static final Character COMMAND_PREFIX = '>';
    private static final Map<String, CommandAndMethod> handlerByCommand = new HashMap<>();

    public static void main(final String[] args) {
        applyConfigurations();

        final String token = getToken();
        final DiscordClient client = DiscordClient.create(token);

        List<CommandService> listeners = Services.getAll(CommandService.class);
        populateHandlerByCommand(listeners);
        client.withGateway(gateway -> {
            gateway.on(MessageCreateEvent.class)
                    .flatMap(event -> Mono.just(event.getMessage().getContent())
                            .flatMap(content -> {
                                        String command = content.split(" ")[0];
                                        return Mono.justOrEmpty(handlerByCommand.get(command))
                                                .flatMap(entry -> {
                                                    try {
                                                        var listener = Services.getService(CommandService.class, entry.getClazz());
                                                        return (Mono<?>) entry.getMethod().invoke(listener, event);
                                                    } catch (Exception e) {
                                                        return Mono.error(e);
                                                    }
                                                })
                                                .doOnError(e -> log.error(e.getMessage()))
                                                .then();
                                    }
                            )
                    )
                    .subscribe(null, (e) -> log.error(e.getMessage(), e));

            return Mono.empty();
        }).block();
    }

    private static void populateHandlerByCommand(List<CommandService> listeners) {
        for (CommandService listener : listeners) {
            for (Method method : listener.getClass().getMethods()) {
                Command command = method.getAnnotation(Command.class);
                if (command != null &&
                        method.getParameterTypes().length > 0 &&
                        method.getParameterTypes()[0] == MessageCreateEvent.class) {

                    final String commandValue = COMMAND_PREFIX + command.value().trim();

                    if (method.getReturnType() != Mono.class) {
                        throw new RuntimeException(format("command's \"{0}\" return type is not Mono"));
                    } else if (handlerByCommand.containsKey(commandValue)) {
                        throw new RuntimeException(format("command \"{0}\" is bound to multiple methods", command.value()));
                    }

                    handlerByCommand.put(commandValue, new CommandAndMethod(listener.getClass(), method));
                }
            }
        }
    }

    private static String getToken() {
        final String token = System.getenv("DISCORD_TOKEN");
        if (isBlank(token))
            throw new RuntimeException("Token not found");

        return token;
    }

    private static void applyConfigurations() {
        Locale.setDefault(new Locale("en", "US"));
    }
}
