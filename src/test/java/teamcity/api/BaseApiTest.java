package teamcity.api;

import io.restassured.RestAssured;
import org.apache.hc.core5.http.HttpStatus;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import teamcity.BaseTest;
import teamcity.api.models.AuthModules;
import teamcity.api.models.ServerAuthSettings;
import teamcity.api.requests.ServerAuthRequest;
import teamcity.api.spec.Specifications;

import static teamcity.api.generators.TestDataGenerator.generate;

public class BaseApiTest extends BaseTest {

    private final ServerAuthRequest serverAuthRequest = new ServerAuthRequest(Specifications.superUserAuth());
    private AuthModules authModules;
    private boolean perProjectPermissions;

    protected String getBuildLog(String buildId) {
        return RestAssured
                .given()
                .spec(Specifications.superUserAuth())
                .get("/downloadBuildLog.html?buildId=" + buildId + "&zipped=false")
                .then()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }

    @BeforeSuite(alwaysRun = true)
    public void setUpServerAuthSettings() {
        // Получаем текущие настройки perProjectPermissions
        perProjectPermissions = serverAuthRequest.read().getPerProjectPermissions();

        authModules = generate(AuthModules.class);
        // Обновляем значение perProjectPermissions на true
        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(true)
                .modules(authModules)
                .build());

    }

    @AfterSuite(alwaysRun = true)
    public void cleanUpServerAuthSettings() {
        // Возвращаем настройке perProjectPermissions исходное значение
        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(perProjectPermissions)
                .modules(authModules)
                .build());
    }

}
