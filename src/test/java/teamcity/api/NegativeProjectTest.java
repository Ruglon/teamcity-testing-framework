package teamcity.api;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import teamcity.api.config.Config;
import teamcity.api.models.*;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;
import teamcity.api.spec.StepGenerator;
import teamcity.api.spec.ValidationResponseSpecifications;

import java.time.Duration;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static teamcity.api.enums.Endpoint.*;
import static teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class NegativeProjectTest extends BaseApiTest {

    @Test(description = "User should be able to create project", groups = {"Positive", "Project"})
    public void userCreatesProjectSuccessfullyTest() {

        superUserCheckedRequests.getRequest(PROJECTS).create(testData.getProject());

        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).read(testData.getProject().getId());

        softy.assertEquals(createdProject, testData.getProject());

    }

    @Test(description = "User should not be able to create project with duplicate id", groups = {"Negative", "Project"})
    public void userCreatesProjectWithDuplicateIdTest() {
        var projectData = testData.getProject();
        superUserCheckedRequests.getRequest(PROJECTS).create(projectData);
        var duplicateNameProject = generate(Project.class);
        duplicateNameProject.setId(projectData.getId());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(duplicateNameProject)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectNameAlreadyExists(projectData.getName()));
    }

    @Test(description = "User should not be able to create project with duplicate name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithDuplicateNameTest() {
        var projectData = testData.getProject();
        superUserCheckedRequests.getRequest(PROJECTS).create(projectData);
        var duplicateNameProject = generate(Project.class);
        duplicateNameProject.setName(projectData.getName());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(duplicateNameProject)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectNameAlreadyExists(projectData.getName()));
    }

    @Test(description = "User should not be able to create project with empty id", groups = {"Negative", "Project"})
    public void userCreatesProjectWithEmptyIdTest() {
        var invalidProject = testData.getProject();
        testData.getProject().setId("");

        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectIdMustNotBeEmpty(invalidProject.getName()));
    }

    @Test(description = "User should not be able to create project without name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithoutNameTest() {
        var project = generate(Project.class);
        project.setName(null);

        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(project)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectNameCannotBeEmpty(project.getName()));
    }

    @Test(description = "User should not be able to create a project if id starts with number", groups = {"Negative", "Project"})
    public void userCreatesProjectWithIdStartingWithNumberTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("123" + invalidProject.getId());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }

    @Test(description = "User should not be able to create a project if id includes invalid symbols", groups = {"Negative", "Project"})
    public void userCreatesProjectWithInvalidSymbolsIdTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("#@!" + invalidProject.getId());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }

    @Test(description = "User should not be able to create a project if id includes cyrillic symbols", groups = {"Negative", "Project"})
    public void userCreatesProjectWithCyrillicIdTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("Проект_" + invalidProject.getId());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }

    @Test(description = "User should not be able to create a project if id has more than 225 symbols", groups = {"Negative", "Project"})
    public void userCreatesProjectWithLongIdTest() {
        var invalidProject = generate(Project.class);
        String longId = "a".repeat(226);
        invalidProject.setId(longId);
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }

    @Test(description = "User should not be able to create a project if id starts with _", groups = {"Negative", "Project"})
    public void userCreatesProjectWithIdStartingWithUnderscoreTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("_" + invalidProject.getId());
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }

    @Test(description = "User should not be able to create a project with empty name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithEmptyNameTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setName("");
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectNameCannotBeEmpty(invalidProject.getName()));
    }

    @Test(description = "User should not be able to create a project with empty id and name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithEmptyIdAndNameTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("");
        invalidProject.setName("");
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkProjectIdMustNotBeEmpty(invalidProject.getName()));
    }

    @Test(description = "User should not be able to create a project with invalid id and empty name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithInvalidIdAndEmptyNameTest() {
        var invalidProject = generate(Project.class);
        invalidProject.setId("#@!" + invalidProject.getId());
        invalidProject.setName("");
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .spec(ValidationResponseSpecifications.checkInvalidProjectId(invalidProject.getId()));
    }


    @Test(description = "User should not be able to create a copy of non existing project", groups = {"Negative", "Project"})
    public void userCreatesCopyOfNonExistingProjectTest() {
        var copyProject = Project.builder()
                .id("test_copy_id_" + System.currentTimeMillis())
                .name("Test Copy Project")
                .parentProject(Project.ProjectLocator.builder().locator("id:_Root").build())
                .sourceProject(Project.ProjectLocator.builder().locator("id:non_existent").build())
                .copyAllAssociatedSettings(true)
                .build();
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(copyProject)
                .then()
                .spec(ValidationResponseSpecifications.checkSourceProjectNotFound("id:non_existent"));
    }

    @Test(description = "User should not be able to create a copy with empty info about source project", groups = {"Negative", "Project"})
    public void userCreatesCopyWithEmptySourceProjectTest() {
        var copyProject = Project.builder()
                .id("test_copy_id_" + System.currentTimeMillis())
                .name("Test Copy Project")
                .parentProject(Project.ProjectLocator.builder().locator("id:_Root").build())
                .sourceProject(Project.ProjectLocator.builder().locator("").build())
                .copyAllAssociatedSettings(true)
                .build();
        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(copyProject)
                .then()
                .spec(ValidationResponseSpecifications.checkSourceProjectLocatorEmpty());
    }

    @Test(description = "User should not be able to create project as Project Viewer", groups = {"Negative", "Project"})
    public void userCannotCreateProjectAsProjectViewerTest() {
        var viewerUser = User.builder()
                .username("viewer_" + System.currentTimeMillis())
                .password("password")
                .roles(Roles.builder()
                        .roles(List.of(Role.builder().roleId("PROJECT_VIEWER").scope("g").build()))
                        .build())
                .build();
        superUserCheckedRequests.<User>getRequest(USERS).create(viewerUser);
        var project = generate(Project.class);
        new UncheckedBase(Specifications.authSpec(viewerUser), PROJECTS)
                .create(project)
                .then()
                .spec(ValidationResponseSpecifications.checkAccessDenied());
    }

    @Test(description = "User should not be able to create project as Project Developer", groups = {"Negative", "Project"})
    public void userCannotCreateProjectAsProjectDeveloperTest() {
        var developerUser = User.builder()
                .username("developer_" + System.currentTimeMillis())
                .password("password")
                .roles(Roles.builder()
                        .roles(List.of(Role.builder().roleId("PROJECT_DEVELOPER").scope("g").build()))
                        .build())
                .build();
        superUserCheckedRequests.<User>getRequest(USERS).create(developerUser);
        var project = generate(Project.class);
        new UncheckedBase(Specifications.authSpec(developerUser), PROJECTS)
                .create(project)
                .then()
                .spec(ValidationResponseSpecifications.checkAccessDenied());
    }

    @Test(description = "User should not be able to create project as Agent Manager", groups = {"Negative", "Project"})
    public void userCannotCreateProjectAsAgentManagerTest() {
        var agentManagerUser = User.builder()
                .username("agentmanager_" + System.currentTimeMillis())
                .password("password")
                .roles(Roles.builder()
                        .roles(List.of(Role.builder().roleId("AGENT_MANAGER").scope("g").build()))
                        .build())
                .build();
        superUserCheckedRequests.<User>getRequest(USERS).create(agentManagerUser);
        var project = generate(Project.class);
        new UncheckedBase(Specifications.authSpec(agentManagerUser), PROJECTS)
                .create(project)
                .then()
                .spec(ValidationResponseSpecifications.checkAccessDenied());
    }


}

