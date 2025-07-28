package teamcity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import teamcity.api.models.*;
import teamcity.api.requests.checked.CheckedBase;
import teamcity.api.spec.Specifications;
import teamcity.common.WireMock;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static teamcity.api.enums.Endpoint.BUILD_QUEUE;
import static teamcity.api.generators.TestDataGenerator.generate;

@Feature("Start build")
public class StartBuildTest extends BaseApiTest {
    @BeforeMethod
    public void setupWireMockServer() {
        Project project = generate(Project.class);
        BuildType buildType = generate(BuildType.class);
        buildType.setProject(project);

        Build fakeBuild = Build.builder()
                .state("finished")
                .status("SUCCESS")
                .buildType(buildType)
                .build();

        WireMock.setupServer(post(BUILD_QUEUE.getUrl()), HttpStatus.SC_OK, fakeBuild);
    }

    @Test(description = "User should be able to start build (with WireMock)", groups = {"Mock"})
    public void userStartsBuildWithWireMockTest() {
        var checkedBuildQueueRequest = new CheckedBase<Build>(Specifications.getSpec()
                .mockSpec(), BUILD_QUEUE);

        var build = checkedBuildQueueRequest.create(Build.builder()
                .buildType(testData.getBuildType())
                .build());

        assertSoftly(softly -> {
            softly.assertThat(build.getState())
                    .as("Build state should be 'finished'")
                    .isEqualTo("finished");

            softly.assertThat(build.getStatus())
                    .as("Build status should be 'SUCCESS'")
                    .isEqualTo("SUCCESS");

            softly.assertThat(build.getBuildType())
                    .as("BuildType should not be null")
                    .isNotNull();
        });
    }


    @SneakyThrows
    @Feature("Run build")
    @Test(description = "User should be able to run mocked echo build and see Hello, world!", groups = {"Mock", "Positive"})
    public void userRunsEchoHelloWorldWithWireMockTest() {
        // Given: Setup mocks
        var buildType = generate(BuildType.class);

        var checkedBuildQueueRequest = new CheckedBase<Build>(
                Specifications.getSpec().mockSpec(), BUILD_QUEUE);

        // When: Start build
        var createdBuild = checkedBuildQueueRequest.create(buildType);

        // Then: Asserts
        assertSoftly(softly -> {
            softly.assertThat(createdBuild.getState()).as("State").isEqualTo("finished");
            softly.assertThat(createdBuild.getStatus()).as("Status").isEqualTo("SUCCESS");
            softly.assertThat(createdBuild.getBuildType()).as("BuildType not null").isNotNull();
            softly.assertThat(createdBuild.getBuildType().getId()).as("BuildType ID match").isEqualTo(buildType.getId());

            var logs = getBuildLog(createdBuild.getId());  // Fixed id
            softly.assertThat(logs).as("Logs contain echo").contains("Hello, world!");
        });

        // Verify: Request sent correctly
        verify(postRequestedFor(urlEqualTo(BUILD_QUEUE.getUrl()))
                .withRequestBody(matchingJsonPath("$.buildType.id", equalTo(buildType.getId()))));
    }



    @AfterMethod(alwaysRun = true)
    public void stopWireMockServer() {
        WireMock.stopServer();
    }

    private String asJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
