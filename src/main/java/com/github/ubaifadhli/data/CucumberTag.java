package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CucumberTag {
    public String name;


    @JsonIgnore
    private String type;
    @JsonIgnore
    private Object location;
}
