package listener.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import reactor.core.publisher.Mono;
import util.MessageUtils;

public class PingPongEventListener implements MessageEventListener {

    @Getter
    private final String command = "ping";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        return MessageUtils.createMessage(event.getMessage().getChannel(), "Pong!");
    }
}

