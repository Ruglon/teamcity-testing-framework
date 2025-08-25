package teamcity.api.models;

import lombok.Data;

@Data
public class TestData {
    private Project project;
    private User user;
    private BuildType buildType;
    private Step step;
}
