package teamcity.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import teamcity.api.anotations.Parameterizable;
import teamcity.api.anotations.Random;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildType extends BaseModel{
    @Random
    @Parameterizable
    private String id;
    @Random
    private String name;
    private Project project;
    private Steps steps;
    @JsonProperty("artifactRules")
    private ArtifactRules artifactRules;
}
