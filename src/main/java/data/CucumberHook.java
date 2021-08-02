package data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class CucumberHook {
    @JsonIgnore
    private String match;

    CucumberResult result;
    List<CucumberEmbedding> embeddings;
}
