package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CucumberFeature {
    private String name;
    private String id;
    private String uri;

    @JsonProperty("elements")
    private List<CucumberScenario> scenarios;
    private List<CucumberTag> tags;


    @JsonIgnore
    private int line;
    @JsonIgnore
    private String description;
    @JsonIgnore
    private String keyword;
}
