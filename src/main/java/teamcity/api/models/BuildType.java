package teamcity.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import teamcity.api.anotations.Random;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildType extends BaseModel{
    private String id;
    @Random
    private String name;
    private Project project;
    private Steps steps;

}
