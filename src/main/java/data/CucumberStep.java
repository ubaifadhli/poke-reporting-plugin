package data;

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
}
