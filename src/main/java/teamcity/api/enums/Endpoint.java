package teamcity.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import teamcity.api.models.*;

@AllArgsConstructor
@Getter
public enum Endpoint {

    BUILD_TYPES("/app/rest/buildTypes", BuildType.class),
    PROJECTS("/app/rest/projects", Project.class),
    USERS("/app/rest/users", User.class),
    BUILDS("/app/rest/builds", Build.class),
    BUILD_QUEUE("/app/rest/buildQueue", Build.class);


    private final String  url;
    private final Class<? extends BaseModel> modelClass;
}
