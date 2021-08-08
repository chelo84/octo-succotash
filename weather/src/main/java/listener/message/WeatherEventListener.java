package listener.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import openweather.OpenWeatherApiResponse;
import org.apache.http.client.utils.URIBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import rest.HttpClientResponseHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static java.text.MessageFormat.format;

@Slf4j
public class WeatherEventListener implements MessageEventListener {

    @Getter
    private final String command = "weather";

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        HttpClient httpClient = event.getClient().rest().getRestResources().getReactorResources().getHttpClient();

        return this.getArguments(event, "Please provide the arguments to search")
                .flatMap(arg ->
                        httpClient.get()
                                .uri(format("http://api.openweathermap.org/data/2.5/weather?q={0}&appid={1}&units=metric", arg, System.getenv("OPEN_WEATHER_API_KEY")))
                                .responseSingle((response, byteBuf) -> HttpClientResponseHandler.readValue(response, byteBuf, OpenWeatherApiResponse.class))
                                .doOnNext(it -> event.getMessage().getChannel().flatMap(
                                        channel -> channel.createMessage(format("{0}\u00B0 in {1}", it.getMain().getTemp(), it.getName()))
                                ).subscribe())
                ).then();
    }
}
