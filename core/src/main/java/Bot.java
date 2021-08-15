import command.message.MessageCommands;
import discord4j.core.DiscordClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class Bot {


    public static void main(final String[] args) {
        applyConfigurations();

        final String token = getToken();
        final DiscordClient client = DiscordClient.create(token);

        client.withGateway(gateway -> {
            MessageCommands.registerCommands(gateway);

            return Mono.empty();
        }).block();
    }

    private static String getToken() {
        final String token = System.getenv("DISCORD_TOKEN");
        if (isBlank(token))
            throw new RuntimeException("Token not found");

        return token;
    }

    private static void applyConfigurations() {
        Locale.setDefault(new Locale("en", "US"));
    }
}
