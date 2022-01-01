package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CucumberFeature {
    @JsonProperty("elements")
    List<CucumberScenario> scenarios;

    @JsonIgnore
    private String tags;

    int line;
    String name;
    String description;
    String id;
    String keyword;
    String uri;
}
