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
public class Main {
    @JsonProperty("temp")
    Double temp;
    @JsonProperty("feels_like")
    Double feelsLike;
    @JsonProperty("temp_min")
    Double tempMin;
    @JsonProperty("temp_max")
    Double tempMax;
}
