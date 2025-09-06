package teamcity.api;

import io.restassured.RestAssured;
import org.apache.hc.core5.http.HttpStatus;
import teamcity.BaseTest;
import teamcity.api.spec.Specifications;

public class BaseApiTest extends BaseTest {

    protected String getBuildLog(String buildId) {
        return RestAssured
                .given()
                .spec(Specifications.superUserAuth())
                .get("/downloadBuildLog.html?buildId=" + buildId + "&zipped=false")
                .then()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }

}
