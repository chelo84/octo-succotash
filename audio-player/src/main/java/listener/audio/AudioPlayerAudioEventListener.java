package listener.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

@Slf4j
public class AudioPlayerAudioEventListener extends AudioEventAdapter {

    private final Queue<AudioTrack> trackQueue;

    public AudioPlayerAudioEventListener(Queue<AudioTrack> trackQueue) {
        this.trackQueue = trackQueue;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.info("trackEnd");
        trackQueue.remove();

        if (endReason.mayStartNext && !trackQueue.isEmpty()) {
            player.playTrack(trackQueue.poll());
        }
    }
}
