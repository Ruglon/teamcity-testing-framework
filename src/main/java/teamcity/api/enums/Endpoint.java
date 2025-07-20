package teamcity.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import teamcity.api.models.BaseModel;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.models.User;

@AllArgsConstructor
@Getter
public enum Endpoint {

    BUILD_TYPES("/app/rest/buildTypes", BuildType.class),
    PROJECTS("/app/rest/projects", Project.class),
    USERS("/app/rest/users", User.class);


    private final String  url;
    private final Class<? extends BaseModel> modelClass;
}
