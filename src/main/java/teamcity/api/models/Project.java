package teamcity.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import teamcity.api.anotations.Random;

@Builder
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends BaseModel{
    @Random
    private String id;
    @Random
    private String name;
    private String locator;
}
