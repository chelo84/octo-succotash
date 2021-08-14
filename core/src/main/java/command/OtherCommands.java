package command;

import event.annotation.OnMessage;
import event.annotation.MessageArgument;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spi.CommandService;
import util.MessageUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static event.annotation.MessageArgument.ArgType.TEXT;

@Slf4j
public class OtherCommands implements CommandService {

    @OnMessage(
            value = "ping",
            description = "Pong"
    )
    public Mono<?> ping(MessageCreateEvent event) {
        return MessageUtils.createMessage(event.getMessage().getChannel(), "Pong!");
    }

    @OnMessage(
            value = "help",
            args = @MessageArgument(value = "command", required = false, type = TEXT),
            description = "Commands info"
    )
    public Mono<?> help(MessageCreateEvent event, Flux<String> arguments) {
        Set<Command> commands = Commands.getInstance().getCommands();
        return arguments.collectList()
                .flatMap(args -> {
                    if (args.isEmpty()) {
                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Commands: " + commands.stream()
                                        .map(Command::getCommand)
                                        .collect(Collectors.joining(", "))
                                )));
                    } else {
                        Optional<Command> command = commands.parallelStream()
                                .filter(c -> c.getCommand().equalsIgnoreCase(args.get(0)))
                                .findFirst();
                        if (command.isEmpty())
                            return Mono.error(new InvalidArgumentsException("Argument " + args.get(0) + " not valid"));

                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createEmbed(spec ->
                                        spec.setTitle("Command info")
                                                .addField("Command", command.map(Command::getCommand).orElse("No name"), true)
                                                .addField("Description", command.map(Command::getDescription).orElse("No description"), false)
                                                .addField("Example", command.map(Command::getExample).orElse("No example"), false)
                                                .setFooter("<> required | () optional", null)
                                                .setTimestamp(Instant.now()))
                                );
                    }
                });
    }
}