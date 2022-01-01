package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CucumberResult {
    @JsonProperty("error_message")
    String errorMessage;

    Long duration;
    String status;
}
