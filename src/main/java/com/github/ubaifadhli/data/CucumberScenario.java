package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CucumberScenario {
    @JsonProperty("start_timestamp")
    Timestamp startTimestamp;

    private List<CucumberTag> tags;

    int line;
    String name;
    String description;
    String id;
    String type;
    String keyword;
    List<CucumberHook> before;
    List<CucumberHook> after;
    List<CucumberStep> steps;

    public boolean hasBefore() {
        return before != null && before.size() > 0;
    }

    public boolean hasAfter() {
        return after != null && after.size() > 0;
    }

    // Custom needed variable
    Timestamp endTimestamp; // Not precise in nanoseconds, because I don't want to.
    Long duration;
    boolean isPassed;

    public Long getFirstBeforeResultDuration() {
        Long duration = before.get(0).getResult().getDuration();
        return duration != null ? duration : 0L;
    }

    public Long getFirstAfterResultDuration() {
        Long duration = after.get(0).getResult().getDuration();
        return duration != null ? duration : 0L;
    }
}
