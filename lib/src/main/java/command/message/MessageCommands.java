package command.message;

import command.CommandAlreadyBoundException;
import command.CommandsAlreadyRegisteredException;
import command.CommandsNotRegisteredException;
import command.InvalidArgumentsException;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spi.CommandService;
import spi.Services;
import util.MessageUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.text.MessageFormat.format;
import static java.util.Objects.nonNull;

@Slf4j
public final class MessageCommands {

    public static final Character COMMAND_PREFIX = '>';
    private static MessageCommands instance;
    @Getter
    private final Set<MessageCommand> messageCommands = new HashSet<>();

    private MessageCommands() {
        doRegisterCommands();
    }

    public static MessageCommands getInstance() {
        if (instance == null)
            throw new CommandsNotRegisteredException();

        return instance;
    }

    public static void registerCommands(GatewayDiscordClient gateway) {
        if (nonNull(instance)) {
            throw new CommandsAlreadyRegisteredException();
        }

        instance = new MessageCommands();
        registerListener(gateway);
    }

    private static void registerListener(GatewayDiscordClient gateway) {
        gateway.on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> instance.getCommand(content)
                                .flatMap(messageCommand -> runCommand(messageCommand, event))
                                .then()
                        )
                        .onErrorResume(InvalidArgumentsException.class, (ex) -> MessageUtils.createMessageAndSend(event.getMessage().getChannel(), ex.getMessage()))
                )
                .subscribe(null, e -> log.error(e.getMessage(), e));
    }

    private static Mono<?> runCommand(MessageCommand messageCommand, MessageCreateEvent event) {
        try {
            var service = Services.getService(CommandService.class, messageCommand.getService());
            Flux<String> arguments = Mono.justOrEmpty(event.getMessage().getContent())
                    .flatMapMany(content -> Flux.fromArray(content.split(" ")))
                    .skip(1)
                    .filter(StringUtils::isNotBlank);

            switch (messageCommand.getParameterTypes().length) {
                case 0:
                    return (Mono<?>) messageCommand.getMethod().invoke(service);
                case 1:
                    return (Mono<?>) messageCommand.getMethod().invoke(service, event);
                case 2:
                    return (Mono<?>) messageCommand.getMethod().invoke(service, event, arguments);
                default:
                    return Mono.error(new InvalidArgumentsException(format("Invalid arguments in command ({0})", messageCommand.getCommand())));
            }
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private void doRegisterCommands() {
        List<CommandService> services = Services.getAll(CommandService.class);

        for (CommandService service : services) {
            for (Method method : service.getClass().getMethods()) {
                final MessageCommand messageCommand = MessageCommand.create(service, method);

                if (nonNull(messageCommand)) {
                    messageCommands.add(Optional.of(messageCommand)
                            .filter(Predicate.not(messageCommands::contains))
                            .orElseThrow(() -> new CommandAlreadyBoundException(messageCommand)));
                }
            }
        }
    }

    public Mono<MessageCommand> getCommand(String text) {
        return Mono.justOrEmpty(this.messageCommands.parallelStream()
                .filter(it -> it.getCommand().equalsIgnoreCase(text.split(" ")[0].replaceFirst(MessageCommands.COMMAND_PREFIX.toString(), "")))
                .findFirst());
    }

}
