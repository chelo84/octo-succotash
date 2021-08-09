package command;

import command.annotation.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import openweather.OpenWeatherApiResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import rest.HttpClientResponseHandler;
import service.CommandService;

import static java.text.MessageFormat.format;

public class WeatherCommands implements CommandService {

    @Command("weather")
    public Mono<?> weather(MessageCreateEvent event) {
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
