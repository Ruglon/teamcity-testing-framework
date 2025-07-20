package teamcity.api;

import org.testng.annotations.Test;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.models.User;
import teamcity.api.requests.CheckedRequests;
import teamcity.api.spec.Specifications;

import java.util.Arrays;

import static io.qameta.allure.Allure.step;
import static teamcity.api.enums.Endpoint.*;
import static teamcity.api.generators.TestDataGenerator.generate;


@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest{
    
        @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
        public void userCreatesBuildTypeTest() {
            var user = generate(User.class);
            superUserCheckedRequests.getRequest(USERS).create(user);
            var userCheckedRequests = new CheckedRequests(Specifications.authSpec(user));
            var project = generate(Project.class);

            project = userCheckedRequests.<Project>getRequest(PROJECTS).create(project);

            var buildType = generate(Arrays.asList(project), BuildType.class);
            userCheckedRequests.getRequest(BUILD_TYPES).create(buildType);

            var createdBuildType = userCheckedRequests.<BuildType>getRequest(BUILD_TYPES).create(buildType);
            softy.assertEquals(buildType.getName(), createdBuildType.getName(), "Build type name is not correct");
}

        @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
        public void userCreatesTwoBuildTypesWithTheSameIdTest() {
            step("Create user");
            step("Create project by user");
            step("Create buildType1 for project by user");
            step("Create buildType2 with same id as buildType1 for project by user");
            step("Check buildType2 was not created with bad request code");
        }

        @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
        public void projectAdminCreatesBuildTypeTest() {
            step("Create user");
            step("Create project");
            step("Grant user PROJECT_ADMIN role in project");

            step("Create buildType for project by user (PROJECT_ADMIN)");
            step("Check buildType was created successfully");
        }

        @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
        public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
            step("Create user1");
            step("Create project1");
            step("Grant user1 PROJECT_ADMIN role in project1");

            step("Create user2");
            step("Create project2");
            step("Grant user2 PROJECT_ADMIN role in project2");

            step("Create buildType for project1 by user2");
            step("Check buildType was not created with forbidden code");
        }
    }

