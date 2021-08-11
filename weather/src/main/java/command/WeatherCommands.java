package command;

import command.annotation.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.extern.slf4j.Slf4j;
import openweather.OpenWeatherApiError;
import openweather.OpenWeatherApiResponse;
import openweather.Weather;
import org.apache.commons.lang3.text.WordUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import rest.HttpClientResponseHandler;
import spi.CommandService;
import util.MessageUtils;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Slf4j
public class WeatherCommands implements CommandService {

    private final String API_URL = "http://api.openweathermap.org";
    private final BiConsumer<OpenWeatherApiResponse, EmbedCreateSpec> template = (resp, spec) -> {
        var weather = resp.getWeather().stream().findFirst();
        spec.setColor(Color.RED)
                .setAuthor(resp.getName() + " - " + resp.getSys().getCountry().toUpperCase(), null, format(API_URL + "/img/wn/{0}.png", weather.map(Weather::getIcon).orElse(EMPTY)))
                .setTitle(
                        weather.map(Weather::getDescription)
                                .map(WordUtils::capitalize)
                                .orElse(EMPTY)
                )
                .addField("Temperature", Math.round(resp.getMain().getTemp()) + "\u00B0C", false)
                .addField("Min.", Math.round(resp.getMain().getTempMin()) + "\u00B0C\n", true)
                .addField("Max.", Math.round(resp.getMain().getTempMax()) + "\u00B0C\n", true);
    };

    @Command("weather")
    public Mono<?> weather(MessageCreateEvent event, Flux<String> arguments) {
        HttpClient httpClient = event.getClient().rest().getRestResources().getReactorResources().getHttpClient();
        return arguments.switchIfEmpty(Mono.error(new InvalidArgumentsException("Please provide the arguments to search")))
                .collect(Collectors.joining("+"))
                .flatMap(arg -> httpClient.get()
                        .uri(format(
                                API_URL +
                                        "/data/2.5/weather?" +
                                        "q={0}&" +
                                        "appid={1}&" +
                                        "units=metric&" +
                                        "lang=en",
                                arg,
                                System.getenv("OPEN_WEATHER_API_KEY")
                        ))
                        .responseSingle((response, byteBuf) -> {
                            if (response.status().code() != 200) {
                                return HttpClientResponseHandler.readValue(response, byteBuf, OpenWeatherApiError.class)
                                        .map(OpenWeatherApiError::getMessage)
                                        .defaultIfEmpty("Error")
                                        .flatMap(msg -> Mono.error(new Exception(msg)));
                            } else {
                                return HttpClientResponseHandler.readValue(response, byteBuf, OpenWeatherApiResponse.class);
                            }
                        })
                        .onErrorResume((e) -> {
                            MessageUtils.createMessageAndSend(event.getMessage().getChannel(), e.getMessage());
                            return Mono.empty();
                        })
                        .doOnNext(resp -> event.getMessage().getChannel().flatMap(channel ->
                                channel.createEmbed(spec ->
                                        template.accept(resp, spec))

                        ).subscribe())).then();
    }
}
