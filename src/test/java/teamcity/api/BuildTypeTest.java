package teamcity.api;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.requests.CheckedRequests;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;

import java.util.Arrays;

import static io.qameta.allure.Allure.step;
import static teamcity.api.enums.Endpoint.*;
import static teamcity.api.generators.TestDataGenerator.generate;


@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest{
    
        @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
        public void userCreatesBuildTypeTest() {
            superUserCheckedRequests.getRequest(USERS).create(testData.getUser());
            var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

            userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

            userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

            var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());
            softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build type name is not correct");
}

        @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
        public void userCreatesTwoBuildTypesWithTheSameIdTest() {
            var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());

            superUserCheckedRequests.getRequest(USERS).create(testData.getUser());

            var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

            userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

            userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
            new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                    .create(buildTypeWithSameId)
                    .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(testData.getBuildType().getId())));
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

