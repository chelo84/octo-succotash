package openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@With
@Value
@Builder
@Jacksonized
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MainDto {
    @JsonProperty("temp")
    double temp;
    @JsonProperty("feels_like")
    double feelsLike;
    @JsonProperty("temp_min")
    double tempMin;
    @JsonProperty("temp_max")
    double tempMax;
}
