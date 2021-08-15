package command;

import audio.track.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection;
import event.annotation.MessageArgument;
import event.annotation.OnMessage;
import guild.AudioPlayerGuildData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spi.CommandService;

import java.util.function.Consumer;

import static event.annotation.MessageArgument.ArgType.INTEGER;
import static event.annotation.MessageArgument.ArgType.TEXT;
import static util.MessageUtils.createMessageAndSend;

public class AudioPlayerMessageCommands implements CommandService {

    @OnMessage(
            value = "join",
            description = "Tells the bot to join the voice channel"
    )
    public Mono<?> join(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .zipWith(
                        Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
                )
                .flatMap(tuple -> {
                    var audioPlayerGD = tuple.getT1();
                    var voiceState = tuple.getT2();
                    return voiceState.getChannel()
                            .flatMap(vs -> vs.join(spec -> spec.setProvider(audioPlayerGD.getProvider())));
                });
    }

    @OnMessage(
            value = "leave",
            description = "Tells the bot to leave the voice channel"
    )
    public Mono<?> leave(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(voiceState -> event.getClient()
                        .getVoiceConnectionRegistry()
                        .getVoiceConnection(voiceState.getGuildId()))
                .flatMap(VoiceConnection::disconnect);
    }

    @OnMessage(
            value = "play",
            args = @MessageArgument(value = "song", type = TEXT),
            description = "Plays a song"
    )
    public Mono<?> play(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .flatMap(audioPlayerGD -> this.getArguments(event, "Send audio after the command")
                        .doOnNext(arg -> {
                            final TrackScheduler scheduler = new TrackScheduler(audioPlayerGD.getPlayer(), audioPlayerGD.getTrackQueue(), event.getMessage().getChannel());
                            audioPlayerGD.getManager().loadItem(arg, scheduler);
                        })
                        .then()
                );
    }

    @OnMessage(
            value = "skip",
            description = "Skips the current song"
    )
    public Mono<?> skip(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .map(AudioPlayerGuildData::getPlayer)
                .doOnNext(AudioPlayer::stopTrack);
    }

    @OnMessage(
            value = "volume",
            args = @MessageArgument(value = "newVolume", type = INTEGER, required = false),
            description = "newVolume absent: current volume\nnewVolume present: set the volume"
    )
    public Mono<?> volume(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .flatMap(audioPlayerGD -> this.getArguments(event)
                        .switchIfEmpty(sendVolumeMessage(event, audioPlayerGD))
                        .map(Integer::parseInt)
                        .doOnNext(setVolume(event, audioPlayerGD))
                        .then()
                ).onErrorResume(
                        NumberFormatException.class,
                        (ex) -> createMessageAndSend(event.getMessage().getChannel(), "Please provide an integer value")
                );
    }

    private Consumer<Integer> setVolume(MessageCreateEvent event, AudioPlayerGuildData audioPlayerGD) {
        return volume -> {
            createMessageAndSend(event.getMessage().getChannel(), "Setting volume to {0}...", volume);
            audioPlayerGD.getPlayer().setVolume(volume);
        };
    }

    private Flux<String> sendVolumeMessage(MessageCreateEvent event, AudioPlayerGuildData audioPlayerGD) {
        return Flux.defer(() -> {
            createMessageAndSend(event.getMessage().getChannel(), "Volume is set to {0}", audioPlayerGD.getPlayer().getVolume());
            return Flux.empty();
        });
    }
}
