package guild;

import discord4j.common.util.Snowflake;

public abstract class GuildData {
    public static <T extends GuildData> T getInstance(Snowflake snowflake) {
        throw new RuntimeException("Please override the static GuildData#from method");
    }
}
