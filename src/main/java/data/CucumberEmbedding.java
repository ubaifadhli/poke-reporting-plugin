package data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CucumberEmbedding {
    @JsonProperty("mime_type")
    String mimeType;

    byte[] data;
    String name;

    // Custom needed variable
    String fileName;
    String dataText;
}
