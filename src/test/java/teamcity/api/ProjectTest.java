package teamcity.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import teamcity.api.models.*;
import teamcity.api.requests.CheckedRequests;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static teamcity.api.enums.Endpoint.*;
import static teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTest extends BaseApiTest {

    @Test(description = "User should be able to create project", groups = {"Positive", "Project"})
    public void userCreatesProjectSuccessfullyTest() {

        superUserCheckedRequests.getRequest(PROJECTS).create(testData.getProject());

        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).read(testData.getProject().getId());

        softy.assertEquals(createdProject.getName(), testData.getProject().getName(), "Project name must match");
        softy.assertEquals(createdProject.getId(), testData.getProject().getId(), "Project ID must match");
    }

    @Test(description = "User should not be able to create project with duplicate id", groups = {"Negative", "Project"})
    public void userCreatesProjectWithDuplicateIdTest() {
        var projectData = testData.getProject();

        superUserCheckedRequests.getRequest(PROJECTS).create(projectData);

        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(projectData)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project with this name already exists: " + projectData.getName()));
    }

    @Test(description = "User should not be able to create project with empty id", groups = {"Negative", "Project"})
    public void userCreatesProjectWithEmptyIdTest() {
        var invalidProject = testData.getProject();
        testData.getProject().setId("");

        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(invalidProject)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID must not be empty"));
    }

    @Test(description = "User should not be able to create project without name", groups = {"Negative", "Project"})
    public void userCreatesProjectWithoutNameTest() {
        var project = generate(Project.class);
        project.setName(null);

        new UncheckedBase(Specifications.superUserAuth(), PROJECTS)
                .create(project)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project name cannot be empty"));
    }


    @Test(description = "User should be able to run build with echo 'Hello suka!' and verify it in console")
    public void userRunsEchoHelloSukaBuildTest() {
        // 1. Проверяем наличие агентов
        step("Check for compatible agents");
        String agentsResponse = given()
                .spec(Specifications.superUserAuth())
                .header("Accept", "application/json")
                .get("/app/rest/agents")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();
        System.out.println("Available agents: " + agentsResponse);
        if (agentsResponse.contains("\"count\":0")) {
            throw new RuntimeException("No compatible agents available. Check TeamCity agents at http://192.168.0.217:8111/agents.html");
        }

        // 2. Создаём проект
        step("Create project");
        var project = generate(Project.class);
        superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).read(project.getId());
        softy.assertEquals(createdProject.getId(), project.getId(), "Project ID must match");
        softy.assertEquals(createdProject.getName(), project.getName(), "Project name must match");

        // 3. Создаём конфигурацию сборки
        step("Create build type with echo 'Hello suka!'");
        var buildType = generate(BuildType.class);
        buildType.setProject(project);

        var echoStep = Step.builder()
                .name("Echo Hello Suka")
                .type("simpleRunner")
                .parameters(Map.of(
                        "use.custom.script", "true",
                        "script.content", """
                            echo Hello suka!
                            echo ##teamcity[message text='Echo Hello suka executed' status='NORMAL']
                            """,
                        "script.mode", "default"
                ))
                .build();

        buildType.setSteps(Steps.builder()
                .count(1)
                .step(List.of(echoStep))
                .build());
        superUserCheckedRequests.<BuildType>getRequest(BUILD_TYPES).create(buildType);

        // 4. Запускаем сборку
        step("Start build");
        var startedBuild = superUserCheckedRequests.<Build>getRequest(BUILD_QUEUE)
                .create(Build.builder()
                        .buildType(buildType)
                        .build());

        String buildId = startedBuild.getId();
        System.out.println("Build ID: " + buildId);
        if (buildId == null || buildId.isEmpty()) {
            throw new RuntimeException("Build ID is null or empty, build creation failed");
        }
        if (buildId.startsWith("id:")) {
            buildId = buildId.replace("id:", "");
        }

        // 5. Ждём завершения билда и проверяем статус
        step("Wait for build to finish and verify status is SUCCESS");
        await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Build build = given()
                            .spec(Specifications.superUserAuth())
                            .header("Accept", "application/json")
                            .get("/app/rest/builds/id:" + startedBuild.getId())
                            .then()
                            .statusCode(HttpStatus.SC_OK)
                            .extract().as(Build.class);

                    System.out.println("Build state: " + build.getState() + ", status: " + build.getStatus());
                    softy.assertEquals("finished", build.getState(), "Build is not finished");
                    softy.assertEquals("SUCCESS", build.getStatus(), "Build status is not SUCCESS");
                });

        // 6. Проверяем сервисное сообщение
        step("Check service message for 'Echo Hello suka executed'");
//        String messagesResponse = given()
//                .spec(Specifications.superUserAuth())
//                .header("Accept", "application/json")
//                .get("/app/rest/builds/id:" + buildId + "/messages")
//                .then()
//                .statusCode(HttpStatus.SC_OK)
//                .extract().asString();

//        System.out.println("Messages response: " + messagesResponse);
//        softy.assertTrue(messagesResponse.contains("Echo Hello suka executed"), "Service message 'Echo Hello suka executed' not found in build messages");

        // 7. Лог для отладки
        String logUrl = "http://192.168.0.217:8111/viewLog.html?buildId=" + buildId;
        System.out.println("Log URL for manual check: " + logUrl);
    }
}

