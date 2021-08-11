package command;

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

import static java.util.Objects.nonNull;

@Slf4j
public final class Commands {

    public static final Character COMMAND_PREFIX = '>';
    private static Commands instance;
    @Getter
    private final Set<Command> commands = new HashSet<>();

    private Commands() {
        doRegisterCommands();
    }

    public static Commands getInstance() {
        if (instance == null)
            throw new CommandsNotRegisteredException();

        return instance;
    }

    public static void registerCommands(GatewayDiscordClient gateway) {
        if (nonNull(instance)) {
            throw new CommandsAlreadyRegisteredException();
        }

        instance = new Commands();
        registerListener(gateway);
    }

    private static void registerListener(GatewayDiscordClient gateway) {
        gateway.on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> instance.getCommand(content)
                                .flatMap(command -> runCommand(command, event))
                                .then()
                        )
                        .onErrorResume(InvalidArgumentsException.class, (ex) -> MessageUtils.createMessageAndSend(event.getMessage().getChannel(), ex.getMessage()))
                )
                .subscribe(null, e -> log.error(e.getMessage(), e));
    }

    private static Mono<?> runCommand(Command command, MessageCreateEvent event) {
        try {
            var service = Services.getService(CommandService.class, command.getService());
            Flux<String> arguments = Mono.justOrEmpty(event.getMessage().getContent())
                    .flatMapMany(content -> Flux.fromArray(content.split(" ")))
                    .skip(1)
                    .filter(StringUtils::isNotBlank);
            return (Mono<?>) command.getMethod().invoke(service, event, arguments);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private void doRegisterCommands() {
        List<CommandService> services = Services.getAll(CommandService.class);

        for (CommandService service : services) {
            for (Method method : service.getClass().getMethods()) {
                final Command command = Command.create(service, method);

                if (nonNull(command)) {
                    commands.add(Optional.of(command)
                            .filter(Predicate.not(commands::contains))
                            .orElseThrow(() -> new CommandAlreadyBoundException(command)));
                }
            }
        }
    }

    public Mono<Command> getCommand(String text) {
        return Mono.justOrEmpty(this.commands.parallelStream()
                .filter(it -> it.getCommand().equalsIgnoreCase(text.split(" ")[0].replaceFirst(Commands.COMMAND_PREFIX.toString(), "")))
                .findFirst());
    }

}
