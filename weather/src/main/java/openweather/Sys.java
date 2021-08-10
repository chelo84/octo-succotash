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
public class Sys {
    @JsonProperty("type")
    Integer type;
    @JsonProperty("id")
    Long id;
    @JsonProperty("country")
    String country;
    @JsonProperty("sunrise")
    Long sunrise;
    @JsonProperty("sunset")
    Long sunset;
}
