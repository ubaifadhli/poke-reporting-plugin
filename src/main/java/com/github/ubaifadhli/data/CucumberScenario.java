package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CucumberScenario {
    @JsonProperty("start_timestamp")
    Timestamp startTimestamp;

    @JsonIgnore
    private String tags;

    int line;
    String name;
    String description;
    String id;
    String type;
    String keyword;
    List<CucumberHook> before;
    List<CucumberHook> after;
    List<CucumberStep> steps;

    // Custom needed variable
    Timestamp endTimestamp; // Not precise in nanoseconds, because i don't want to
    Long duration;
    boolean isPassed;

}
