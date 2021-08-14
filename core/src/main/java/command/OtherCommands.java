package command;

import command.annotation.Command;
import command.annotation.CommandArg;
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

import static command.annotation.CommandArg.ArgType.TEXT;

@Slf4j
public class OtherCommands implements CommandService {

    @Command(
            value = "ping",
            description = "Pong"
    )
    public Mono<?> ping(MessageCreateEvent event) {
        return MessageUtils.createMessage(event.getMessage().getChannel(), "Pong!");
    }

    @Command(
            value = "help",
            args = @CommandArg(value = "command", required = false, type = TEXT),
            description = "Commands info"
    )
    public Mono<?> help(MessageCreateEvent event, Flux<String> arguments) {
        Set<Cmd> commands = Commands.getInstance().getCommands();
        return arguments.collectList()
                .flatMap(args -> {
                    if (args.isEmpty()) {
                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Commands: " + commands.stream()
                                        .map(Cmd::getCommand)
                                        .collect(Collectors.joining(", "))
                                )));
                    } else {
                        Optional<Cmd> command = commands.parallelStream()
                                .filter(c -> c.getCommand().equalsIgnoreCase(args.get(0)))
                                .findFirst();
                        if (command.isEmpty())
                            return Mono.error(new InvalidArgumentsException("Argument " + args.get(0) + " not valid"));

                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createEmbed(spec ->
                                        spec.setTitle("Command info")
                                                .addField("Command", command.map(Cmd::getCommand).orElse("No name"), true)
                                                .addField("Description", command.map(Cmd::getDescription).orElse("No description"), false)
                                                .addField("Example", command.map(Cmd::getExample).orElse("No example"), false)
                                                .setFooter("<> required | () optional", null)
                                                .setTimestamp(Instant.now()))
                                );
                    }
                });
    }
}