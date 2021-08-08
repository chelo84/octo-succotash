package audio.track;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import util.MessageUtils;

import java.util.LinkedList;
import java.util.Queue;

import static java.text.MessageFormat.format;
import static util.MessageUtils.createMessage;
import static util.MessageUtils.createMessageAndSend;

@RequiredArgsConstructor
public final class TrackScheduler implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private final LinkedList<AudioTrack> trackQueue;
    private final Mono<MessageChannel> messageChannel;

    @Override
    public void trackLoaded(AudioTrack track) {
        boolean wasEmpty = trackQueue.isEmpty();

        trackQueue.add(track);

        int place = trackQueue.indexOf(track) + 1;
        createMessageAndSend(
                messageChannel,
                "Track \"{0}\" added to the queue at position {1}", track.getInfo().title, place
        );

        if (wasEmpty) {
            player.playTrack(track);
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {
        createMessageAndSend(messageChannel, "Track not found");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        createMessageAndSend(messageChannel, format("Failed to load track {}", exception.getMessage()));
    }
}
