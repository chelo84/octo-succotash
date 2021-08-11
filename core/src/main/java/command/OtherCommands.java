package command;

import command.annotation.Command;
import command.annotation.CommandArg;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spi.CommandService;
import util.MessageUtils;

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
        Set<command.Command> commands = Commands.getInstance().getCommands();
        return arguments.collectList()
                .flatMap(args -> {
                    if (args.isEmpty()) {
                        return event.getMessage().getChannel()
                                .flatMap(channel -> channel.createMessage(spec -> spec.setContent(commands.stream()
                                        .map(command.Command::getCommand)
                                        .collect(Collectors.joining(", "))
                                )));
                    } else {
                        return MessageUtils.createMessage(event.getMessage().getChannel(), "a");
                    }
                });
    }
}