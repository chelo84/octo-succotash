package guild;

import audio.provider.LavaPlayerAudioProvider;
import audio.track.TrackScheduler;
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
import java.util.Queue;

import static java.util.Objects.isNull;

@Getter
public class AudioPlayerGuildData extends GuildData {
    private static final Map<Snowflake, AudioPlayerGuildData> gdMap = new HashMap<>();

    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private final AudioProvider provider;
    private final LinkedList<AudioTrack> trackQueue = new LinkedList<>();

    public AudioPlayerGuildData(Snowflake snowflake) {
        manager = createAudioPlayerManager();
        player = createAudioPlayer(manager);
        provider = createAudioProvider(player);

        gdMap.put(snowflake, this);
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
        return new LavaPlayerAudioProvider( player );
    }

    public static AudioPlayerGuildData getInstance(Snowflake snowflake) {
        AudioPlayerGuildData guildData = gdMap.get(snowflake);
        if (isNull(guildData)) {
            return new AudioPlayerGuildData(snowflake);
        }

        return guildData;
    }
}
