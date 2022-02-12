package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class CucumberStep {
    private String name;
    private CucumberResult result;

    private List<CucumberHook> after;


    @JsonIgnore
    private String match;
    @JsonIgnore
    private int line;
    @JsonIgnore
    private String keyword;
    @JsonIgnore
    private List<CucumberHook> before;
    @JsonIgnore
    private List<Object> rows;
}
