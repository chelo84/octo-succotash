package listener.message;

import audio.track.TrackScheduler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import guild.AudioPlayerGuildData;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class PlayEventListener implements MessageEventListener {

    @Getter
    private final String command = "play";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {

        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .flatMap(audioPlayerGD -> this.getArguments(event, "Send audio after the command")
                        .doOnNext(arg -> {
                            final TrackScheduler scheduler = new TrackScheduler( audioPlayerGD.getPlayer(), audioPlayerGD.getTrackQueue(), event.getMessage().getChannel() );
                            audioPlayerGD.getManager().loadItem(arg, scheduler);
                        })
                        .then()
                );
    }
}
