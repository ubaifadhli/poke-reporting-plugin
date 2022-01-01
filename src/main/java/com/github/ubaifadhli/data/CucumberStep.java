package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class CucumberStep {
    @JsonIgnore
    private String match;

    int line;
    String name;
    String keyword;
    CucumberResult result;
    List<CucumberHook> before;
    List<CucumberHook> after;

    public boolean hasBefore() {
        return before != null && before.size() > 0;
    }

    public boolean hasAfter() {
        return after != null && after.size() > 0;
    }

    public Long getFirstBeforeResultDuration() {
        Long duration = before.get(0).getResult().getDuration();
        return duration != null ? duration : 0L;
    }

    public Long getFirstAfterResultDuration() {
        Long duration = after.get(0).getResult().getDuration();
        return duration != null ? duration : 0L;
    }
}
