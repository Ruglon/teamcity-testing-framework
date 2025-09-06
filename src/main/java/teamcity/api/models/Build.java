package teamcity.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import teamcity.api.anotations.Parameterizable;
import teamcity.api.anotations.Random;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Build extends BaseModel {
    @Random
    @Parameterizable
    private String id;
    private String state;
    private String status;
    private BuildType buildType;
    private Boolean personal;
    private Comment comment;
}
