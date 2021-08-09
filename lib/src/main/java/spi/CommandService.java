package spi;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommandService extends Service {

    default Flux<String> getArguments(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .flatMapMany(content -> Flux.fromArray(content.split(" ")))
                .skip(1);
    }

    default Flux<String> getArguments(MessageCreateEvent event, String noArgsMessage) {
        return getArguments(event)
                .switchIfEmpty(Mono.defer(() -> sendMessageNoArgs(event, noArgsMessage)));
    }

    default Mono<String> sendMessageNoArgs(MessageCreateEvent event, String noArgsMessage) {
        event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(noArgsMessage))
                .subscribe();
        return Mono.empty();
    }
}
