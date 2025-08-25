package teamcity.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import teamcity.api.anotations.Random;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step extends BaseModel{
    @Random
    private String id;
    @Random
    private String name;
    @Builder.Default
    private String type = "simpleRunner";

    private Map<String, String> parameters;

}
