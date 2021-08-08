package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientResponseHandler {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> Mono<T> readValue(HttpClientResponse response, ByteBufMono byteBufMono, Class<T> clazz) {
        return byteBufMono.asString(StandardCharsets.UTF_8)
                .flatMap(it -> {
                    try {
                        return Mono.justOrEmpty(objectMapper.readValue(it, clazz));
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }
}
