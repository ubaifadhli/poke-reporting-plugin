package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CucumberScenario {
    private String id;
    private String name;
    private String keyword;

    @JsonProperty("start_timestamp")
    private Timestamp startTimestamp;

    private List<CucumberTag> tags;
    private List<CucumberStep> steps;
    private List<CucumberHook> after;
    private List<CucumberHook> before;


    @JsonIgnore
    private int line;
    @JsonIgnore
    private String type;
    @JsonIgnore
    private String description;
}
