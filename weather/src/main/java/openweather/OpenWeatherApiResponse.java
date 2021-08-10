package openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

@With
@Value
@Builder
@Jacksonized
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherApiResponse {
    @JsonProperty("main")
    Main main;
    @JsonProperty("name")
    String name;
    @JsonProperty("coor")
    Coord coord;
    @JsonProperty("clouds")
    Clouds clouds;
    @JsonProperty("sys")
    Sys sys;
    @Builder.Default
    @JsonProperty("weather")
    List<Weather> weather = new ArrayList<>();
    @JsonProperty("wind")
    Wind wind;
    @JsonProperty("timezone")
    Integer timezone;
}
