package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CucumberResult {
    private Long duration;
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;
}
