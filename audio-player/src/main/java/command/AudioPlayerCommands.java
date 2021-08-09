package command;

import audio.track.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import command.annotation.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection;
import guild.AudioPlayerGuildData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.CommandService;
import util.MessageUtils;

import static util.MessageUtils.createMessageAndSend;

public class AudioPlayerCommands implements CommandService {

    @Command("test")
    public Mono<?> test(MessageCreateEvent event) {
        return MessageUtils.createMessage(event.getMessage().getChannel(), "Pong!");
    }

    @Command("join")
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

    @Command("leave")
    public Mono<?> leave(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(voiceState -> event.getClient()
                        .getVoiceConnectionRegistry()
                        .getVoiceConnection(voiceState.getGuildId()))
                .flatMap(VoiceConnection::disconnect);
    }

    @Command("play")
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

    @Command("skip")
    public Mono<?> skip(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .map(AudioPlayerGuildData::getPlayer)
                .doOnNext(AudioPlayer::stopTrack);
    }

    @Command("volume")
    public Mono<?> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getGuildId())
                .map(AudioPlayerGuildData::getInstance)
                .flatMap(audioPlayerGD -> this.getArguments(event)
                        .switchIfEmpty(Flux.defer(() -> {
                            createMessageAndSend(event.getMessage().getChannel(), "Volume is set to {0}", audioPlayerGD.getPlayer().getVolume());
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
