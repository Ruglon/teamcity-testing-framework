package teamcity.api;

import io.qameta.allure.Feature;
import org.apache.hc.core5.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import teamcity.api.models.Build;
import teamcity.api.models.BuildType;
import teamcity.api.models.Project;
import teamcity.api.requests.checked.CheckedBase;
import teamcity.api.spec.Specifications;
import teamcity.common.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static teamcity.api.enums.Endpoint.BUILD_QUEUE;
import static teamcity.api.generators.TestDataGenerator.generate;

@Feature("Start build")
public class StartBuildTest extends BaseApiTest {
    @BeforeMethod
    public void setupWireMockServer() {
        // Создай nested buildType
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

    @Test(description = "User should be able to start build (with WireMock)", groups = {"Regression"})
    public void userStartsBuildWithWireMockTest() {
        var checkedBuildQueueRequest = new CheckedBase<Build>(
                Specifications.getSpec().mockSpec(), BUILD_QUEUE);

        var requestBuild = Build.builder()
                .buildType(testData.getBuildType())
                .build();

        var createdBuild = checkedBuildQueueRequest.create(requestBuild);

        assertSoftly(softly -> {
            softly.assertThat(createdBuild.getState())
                    .as("Build state should be 'finished'")
                    .isEqualTo("finished");

            softly.assertThat(createdBuild.getStatus())
                    .as("Build status should be 'SUCCESS'")
                    .isEqualTo("SUCCESS");

            softly.assertThat(createdBuild.getBuildType())
                    .as("BuildType should not be null")
                    .isNotNull();
        });
    }



    @AfterMethod(alwaysRun = true)
    public void stopWireMockServer() {
        WireMock.stopServer();
    }
}
