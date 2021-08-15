package command;

import command.message.MessageCommand;
import command.message.MessageCommands;
import discord4j.core.event.domain.message.MessageCreateEvent;
import event.annotation.MessageArgument;
import event.annotation.OnMessage;
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
public class CoreMessageCommands implements CommandService {

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
        Set<MessageCommand> messageCommands = MessageCommands.getInstance().getMessageCommands();
        return arguments.collectList()
                .flatMap(args -> {
                    if (args.isEmpty()) {
                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Commands: " + messageCommands.stream()
                                        .map(MessageCommand::getCommand)
                                        .collect(Collectors.joining(", "))
                                )));
                    } else {
                        Optional<MessageCommand> command = messageCommands.parallelStream()
                                .filter(c -> c.getCommand().equalsIgnoreCase(args.get(0)))
                                .findFirst();
                        if (command.isEmpty())
                            return Mono.error(new InvalidArgumentsException("Argument " + args.get(0) + " not valid"));

                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createEmbed(spec ->
                                        spec.setTitle("Command info")
                                                .addField("Command", command.map(MessageCommand::getCommand).orElse("No name"), true)
                                                .addField("Description", command.map(MessageCommand::getDescription).orElse("No description"), false)
                                                .addField("Example", command.map(MessageCommand::getExample).orElse("No example"), false)
                                                .setFooter("<> required | () optional", null)
                                                .setTimestamp(Instant.now()))
                                );
                    }
                });
    }
}