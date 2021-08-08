package listener.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import guild.AudioPlayerGuildData;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static util.MessageUtils.createMessage;
import static util.MessageUtils.createMessageAndSend;

public class VolumeEventListener implements MessageEventListener {

    @Getter
    private final String command = "volume";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .flatMap(audioPlayerGD -> this.getArguments(event)
                        .switchIfEmpty(Flux.defer(() -> {
                            createMessageAndSend(event.getMessage().getChannel() , "Volume is set to {0}", audioPlayerGD.getPlayer().getVolume());
                            return Flux.empty();
                        }))
                        .map(Integer::parseInt)
                        .doOnNext(volume -> {
                            createMessageAndSend(event.getMessage().getChannel(), "Setting volume to {0}...", volume);
                            audioPlayerGD.getPlayer().setVolume(volume);
                        })
                        .then()
                ).onErrorResume(
                        NumberFormatException.class,
                        (ex) -> createMessageAndSend(event.getMessage().getChannel(), "Please provide an integer value")
                );
    }
}
