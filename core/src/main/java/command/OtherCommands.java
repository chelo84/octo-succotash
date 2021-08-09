package command;

import command.annotation.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import service.CommandService;
import util.MessageUtils;

public class OtherCommands implements CommandService {

    @Command("ping")
    public Mono<?> execute(MessageCreateEvent event) {
        return MessageUtils.createMessage(event.getMessage().getChannel(), "Pong!");
    }
}
