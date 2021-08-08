import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import listener.message.MessageEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.EventListeners;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class Bot {

    private static final Character COMMAND_PREFIX = '>';

    public static void main(final String[] args) {
        applyConfigurations();

        log.info(System.getenv("DISCORD_TOKEN"));
        final String token = getToken();
        final DiscordClient client = DiscordClient.create(token);

        client.withGateway(gateway -> {
            gateway.on(MessageCreateEvent.class)
                    .flatMap(event -> Mono.just(event.getMessage().getContent())
                            .flatMap(content -> Flux.fromStream(EventListeners.getAll(MessageEventListener.class).stream())
                                    .filter(listener -> StringUtils.startsWithIgnoreCase(content, COMMAND_PREFIX + listener.getCommand()))
                                    .flatMap(listener -> listener.execute(event))
                                    .doOnError(e -> log.error(e.getMessage()))
                                    .next()
                            )
                    )
                    .subscribe(null, (e) -> log.error(e.getMessage(), e));

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
