package guild;

import audio.provider.LavaPlayerAudioProvider;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.voice.AudioProvider;
import listener.audio.AudioPlayerAudioEventListener;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.util.Objects.isNull;

@Getter
public class AudioPlayerData {

    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private final AudioProvider provider;
    private final LinkedList<AudioTrack> trackQueue = new LinkedList<>();

    public AudioPlayerData() {
        manager = createAudioPlayerManager();
        player = createAudioPlayer(manager);
        provider = createAudioProvider(player);
    }

    private AudioPlayerManager createAudioPlayerManager() {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);

        return playerManager;
    }

    private AudioPlayer createAudioPlayer(AudioPlayerManager manager) {
        var player = manager.createPlayer();
        player.addListener(new AudioPlayerAudioEventListener(trackQueue));

        return player;
    }

    private AudioProvider createAudioProvider(AudioPlayer player) {
        return new LavaPlayerAudioProvider(player);
    }
}
