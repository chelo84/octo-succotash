package util;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import static java.text.MessageFormat.format;

public final class MessageUtils {

    public static Mono<Void> createMessageAndSend(Mono<MessageChannel> channelMono, String message, Object... messageArguments) {
        channelMono.flatMap(channel -> channel.createMessage( format(message, messageArguments) )).subscribe();
        return Mono.empty();
    }

    public static Mono<Message> createMessage(Mono<MessageChannel> channelMono, String message, Object... messageArguments) {
        return channelMono.flatMap(channel -> channel.createMessage( format(message, messageArguments) ));
    }
}
