package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class CucumberHook {
    private CucumberResult result;

    private List<CucumberEmbedding> embeddings;


    @JsonIgnore
    private Object match;

    // Inevitable custom variables because of current handler's structure.
    @JsonIgnore
    private String logText;
    @JsonIgnore
    private String filename;

    public CucumberEmbedding getFirstEmbedding() {
        return embeddings.get(0);
    }
}
