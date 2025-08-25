package teamcity.ui;

import com.codeborne.selenide.Condition;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import teamcity.api.enums.Endpoint;
import teamcity.api.generators.TestDataGenerator;
import teamcity.api.models.Project;
import teamcity.api.models.Step;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;
import teamcity.ui.pages.*;
import teamcity.ui.pages.admin.CreateBuildConfigFromURL;
import teamcity.ui.pages.admin.CreateBuildConfigurationPage;
import teamcity.ui.pages.admin.CreateProjectPage;

import static io.qameta.allure.Allure.step;

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


        // проверка состояния API
        // (корректность отправки данных с UI на API)
        step("Check that all entities (project, build type) was successfully created with correct data on API level");

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Check that project is visible on Projects Page (http://localhost:8111/favorite/projects)");

        // взаимодействие с UI
        step("Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)");
        step("Send all project parameters (repository URL)");
        step("Click `Proceed`");
        step("Fix Project Name and Build Type name values");
        step("Click `Proceed`");

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        step("Check that all entities (project, build type) was successfully created with correct data on API level");

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Check that project is visible on Projects Page (http://localhost:8111/favorite/projects)");
    }

    @Test(description = "User should be able to create build configuration", groups = {"Positive"})
    public void userCreatesProjectBuildConfiguration() {

        loginAs(testData.getUser());

        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        var createdProject = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS).read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject);

        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));

        ProjectPage.addNewBuildConfiguration();

        CreateBuildConfigurationPage.getCurrentPage()
                        .createFormBuild(REPO_URL);

        CreateBuildConfigFromURL.getCurrentPage()
                        .checkRequiredFieldsNotEmpty();
        CreateBuildConfigFromURL.getCurrentPage()
                .clickProceedBtn();


        softy.assertTrue(ProjectPage.getCurrentPage().buildStepsActive());

    }


    @Test(description = "User should be able to create build and add new steps with echo \"Hello, World!\"", groups = {"Positive"})
    public void userCreatesProjectWithCommandLineBuildStep() {

        var uncheckedProjects = new UncheckedBase(Specifications.superUserAuth(), Endpoint.PROJECTS);
        var projectsResponse = uncheckedProjects.read("");
        projectsResponse.then().assertThat().statusCode(HttpStatus.SC_OK);

        var firstProjectName = projectsResponse.jsonPath().getString("project[1].name");
        var firstProjectId = projectsResponse.jsonPath().getString("project[1].id");
        softy.assertNotNull(firstProjectName, "First project name should not be null");
        step("Fetched first project name: " + firstProjectName);

        var uncheckedBuildTypes = new UncheckedBase(Specifications.superUserAuth(), Endpoint.BUILD_TYPES);
        var buildTypesResponse = uncheckedBuildTypes.read("?locator=project:" + firstProjectId);
        buildTypesResponse.then().assertThat().statusCode(HttpStatus.SC_OK);

        var firstBuildTypeId = buildTypesResponse.jsonPath().getString("buildType[0].id");
        softy.assertNotNull(firstBuildTypeId, "First build type name should not be null");
        var step = TestDataGenerator.generate(Step.class);

        step("Fetched first build type name: " + firstBuildTypeId);

        loginAs(testData.getUser());

        EditBuildPage.openEditBuildRunners(firstBuildTypeId)
                        .createNewBuildSteps(step.getName(), step.getId(), "echo \"Hello, World!\"");

        BuildConfigPage.getCurrentPage()
                        .checkThatStepBuildCreated(step.getName(), "echo \"Hello, World!\"");


    }


    @Test(description = "User should not be able to craete project without name", groups = {"Negative"})
    public void userCreatesProjectWithoutName() {

        // подготовка окружения
        step("Login as user");
        loginAs(testData.getUser());

        step("Check number of projects");
        var projectsCount = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS)
                .read("").getCount();

        // взаимодействие с UI
        step("Open `Create Project Page`[](http://localhost:8111/admin/createObjectMenu.html)");
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject("", testData.getBuildType().getName());

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        step("Check that number of projects did not change");
        var finalProjectsCount = superUserCheckedRequests.<Project>getRequest(Endpoint.PROJECTS)
                .read("").getCount();
        softy.assertEquals(projectsCount, finalProjectsCount);

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Check that error appears `Project name must not be empty`");
        ProjectsPage.validateErrorMessage();



        // подготовка окружения
        step("Login as user");
        step("Check number of projects");

        // взаимодействие с UI
        step("Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)");
        step("Send all project parameters (repository URL)");
        step("Click `Proceed`");
        step("Set Project Name");
        step("Click `Proceed`");

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        step("Check that number of projects did not change");

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Check that error appears `Project name must not be empty`");
    }
}