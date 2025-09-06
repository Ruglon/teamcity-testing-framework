package teamcity.api;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import teamcity.api.config.Config;
import teamcity.api.models.Build;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.models.Steps;
import teamcity.api.spec.Specifications;
import teamcity.api.spec.StepGenerator;

import java.time.Duration;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static teamcity.api.enums.Endpoint.*;
import static teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class RunProjectTest extends BaseApiTest {

    @Test(description = "User should be able to run build with echo 'Hello, world!' and verify it in console")
    public void userRunsEchoHelloWorldBuildTest() {

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
            throw new RuntimeException(String.format("No compatible agents available. Check TeamCity agents at %s", Config.getProperty("urlCheck")));
        }

        step("Create project");
        var project = generate(Project.class);
        superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).read(project.getId());
        softy.assertEquals(createdProject, project, "Project must match");

        step("Create build type with echo 'Hello, world!'");
        var buildType = generate(BuildType.class);
        buildType.setProject(project);
        var echoStep = StepGenerator.generateSimpleRunner("echo \"Hello, world!\"");
        buildType.setSteps(Steps.builder()
                .count(1)
                .step(List.of(echoStep))
                .build());
        superUserCheckedRequests.<BuildType>getRequest(BUILD_TYPES).create(buildType);

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
        final String finalBuildId = buildId.startsWith("id:") ? buildId.replace("id:", "") : buildId;

        step("Wait for build to finish and verify status is SUCCESS");
        await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Build build = given()
                            .spec(Specifications.superUserAuth())
                            .header("Accept", "application/json")
                            .get("/app/rest/builds/id:" + finalBuildId)
                            .then()
                            .statusCode(HttpStatus.SC_OK)
                            .extract().as(Build.class);

                    System.out.println("Build state: " + build.getState() + ", status: " + build.getStatus());
                    softy.assertEquals("finished", build.getState(), "Build is not finished");
                    softy.assertEquals("SUCCESS", build.getStatus(), "Build status is not SUCCESS");
                });

        step("Verify 'Hello, world!' in build log");
        String buildLog = given()
                .spec(Specifications.superUserAuth())
                .header("Accept", "text/plain")
                .get("/httpAuth/downloadBuildLog.html?buildId=" + finalBuildId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        System.out.println("Build log: " + buildLog);
        softy.assertTrue(buildLog.contains("Hello, world!"), "Build log does not contain 'Hello, world!'");

        String logUrl = "http://192.168.0.217:8111/viewLog.html?buildId=" + finalBuildId;
        System.out.println("Log URL for manual check: " + logUrl);
    }
}
