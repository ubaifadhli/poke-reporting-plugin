package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;

@Data
public class CucumberResult {
    @JsonProperty("error_message")
    String errorMessage;

    Long duration;
    String status;
}
