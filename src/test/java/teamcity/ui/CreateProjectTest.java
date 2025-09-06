package teamcity.ui;

import com.codeborne.selenide.Condition;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import teamcity.api.enums.Endpoint;
import teamcity.api.generators.TestDataGenerator;
import teamcity.api.models.Build;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.models.Step;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;
import teamcity.ui.pages.*;
import teamcity.ui.pages.admin.CreateBuildConfigFromURL;
import teamcity.ui.pages.admin.CreateBuildConfigurationPage;
import teamcity.ui.pages.admin.CreateProjectPage;

import static io.qameta.allure.Allure.step;
import static teamcity.api.enums.Endpoint.BUILD_TYPES;

@Test(groups = {"Regression"})
public class CreateProjectTest extends BaseUiTest {
    private static final String REPO_URL = "https://github.com/Ruglon/teamcity-testing-framework";

    @Test(description = "User should be able to create project", groups = {"Positive"})
    public void userCreatesProject() {
        // подготовка окружения
        loginAs(testData.getUser());

        // взаимодействие с UI
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        var createdProject = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS).read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject);

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        System.out.println("Open the project:");
        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));

        var foundProjects = ProjectsPage.open()
                .getProjects().stream()
                .anyMatch(project -> project.getName().text().equals(testData.getProject().getName()));

        softy.assertTrue(foundProjects);

    }

    @Test(description = "User should be able to create build configuration", groups = {"Positive"})
    public void userCreatesProjectBuildConfiguration() {

        loginAs(testData.getUser());
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        var createdProject = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS)
                .read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject, "Project creation failed");

        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));
        ProjectPage.addNewBuildConfiguration();

        var buildData = TestDataGenerator.generate(Build.class);
        CreateBuildConfigurationPage.getCurrentPage()
                .createFormBuild(REPO_URL);
        var currentBuildConfigPage = CreateBuildConfigFromURL.getCurrentPage();
        currentBuildConfigPage.checkRequiredFieldsNotEmpty(buildData.getBuildType().getName());
        currentBuildConfigPage.clickProceedBtn();

        if (currentBuildConfigPage.checkIfDuplicate()) {
            currentBuildConfigPage.clickCreateDuplicateBtn();
        }

        softy.assertTrue(ProjectPage.getCurrentPage().buildStepsActive(), "Build steps not active");

        BuildType createdBuildType = superUserCheckedRequests.<BuildType>getRequest(BUILD_TYPES)
                .read("name:" + buildData.getBuildType().getName());
        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(),
                "Build type name mismatch between test data and API");

    }


    @Test(description = "User should be able to create build and add new steps with echo \"Hello, World!\"", groups = {"Positive"})
    public void userCreatesProjectWithCommandStep() {
        var unchecked = new UncheckedBase(Specifications.superUserAuth(), Endpoint.PROJECTS);

        var projectsResponse = unchecked.read("");
        projectsResponse.then().assertThat().statusCode(HttpStatus.SC_OK);
        String firstProjectName = projectsResponse.jsonPath().getString("project[1].name");
        String firstProjectId = projectsResponse.jsonPath().getString("project[1].id");
        softy.assertNotNull(firstProjectName, "First project name");

        var buildTypesResponse = unchecked.read("?locator=project:" + firstProjectId);
        buildTypesResponse.then().assertThat().statusCode(HttpStatus.SC_OK);
        String firstBuildTypeId = buildTypesResponse.jsonPath().getString("buildType[0].id");
        softy.assertNotNull(firstBuildTypeId, "First build type ID"); // Records failure but doesn't stop test yet

        if (firstBuildTypeId == null) {
            step("No existing build type found, creating a new one via API");
            var buildData = TestDataGenerator.generate(Build.class);
            buildData.getBuildType().setProject(new Project(firstProjectId, firstProjectName, null, null)); // Link to existing project
            var createdBuildType = superUserCheckedRequests.<BuildType>getRequest(Endpoint.BUILD_TYPES)
                    .create(buildData.getBuildType());
            firstBuildTypeId = createdBuildType.getId();
            softy.assertNotNull(firstBuildTypeId, "Newly created build type ID");
            org.testng.Assert.assertNotNull(firstBuildTypeId, "Critical: Failed to create build type via API");
        }

        var step = TestDataGenerator.generate(Step.class);

        loginAs(testData.getUser());
        EditBuildPage.openEditBuildRunners(firstBuildTypeId)
                .createNewBuildSteps(step.getName(), step.getId(), "echo \"Hello, World!\"");
        BuildConfigPage.getCurrentPage()
                .checkThatStepBuildCreated(step.getName(), "echo \"Hello, World!\"");

        var buildTypeStepsResponse = new UncheckedBase(Specifications.superUserAuth(), Endpoint.BUILD_TYPES)
                .read(firstBuildTypeId + "/steps");
        buildTypeStepsResponse.then().assertThat().statusCode(HttpStatus.SC_OK);

        String createdStepName = buildTypeStepsResponse.jsonPath().getString("step.find { it.name == '" + step.getName() + "' }.name");
        String createdStepScript = buildTypeStepsResponse.jsonPath().getString("step.find { it.name == '" + step.getName() + "' }.properties.property.find { it.name == 'script.content' }.value");
        softy.assertEquals(createdStepName, step.getName(), "Step name mismatch");
        softy.assertEquals(createdStepScript, "echo \"Hello, World!\"", "Step script content mismatch");
    }


    @Test(description = "User should not be able to craete project without name", groups = {"Negative"})
    public void userCreatesProjectWithoutName() {

        step("Login as user");
        loginAs(testData.getUser());

        step("Check number of projects");
        var projectsCount = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS)
                .read("").getCount();


        step("Open `Create Project Page`[](http://localhost:8111/admin/createObjectMenu.html)");
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject("", testData.getBuildType().getName());


        step("Check that number of projects did not change");
        var finalProjectsCount = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS)
                .read("").getCount();
        softy.assertEquals(projectsCount, finalProjectsCount);


        step("Check that error appears `Project name must not be empty`");
        ProjectsPage.validateErrorMessage();


    }
}