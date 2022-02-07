package com.github.ubaifadhli.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CucumberEmbedding {
    private byte[] data;

    @JsonProperty("mime_type")
    private String mimeType;


    @JsonIgnore
    private String name;
}
