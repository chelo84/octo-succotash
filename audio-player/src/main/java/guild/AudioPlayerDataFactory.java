package guild;

import discord4j.common.util.Snowflake;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class AudioPlayerDataFactory {
    private static final Map<Snowflake, AudioPlayerData> byGuild = new HashMap<>();

    public static AudioPlayerData byGuild(Snowflake guildSnowflake) {
        AudioPlayerData guildData = byGuild.get(guildSnowflake);
        if (isNull(guildData)) {
            guildData = new AudioPlayerData();
            byGuild.put(guildSnowflake, guildData);
        }

        return guildData;
    }
}
