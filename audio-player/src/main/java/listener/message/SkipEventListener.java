package listener.message;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import guild.AudioPlayerGuildData;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class SkipEventListener implements MessageEventListener {

    @Getter
    private final String command = "skip";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .map(AudioPlayerGuildData::getPlayer)
                .doOnNext(AudioPlayer::stopTrack);
    }
}
