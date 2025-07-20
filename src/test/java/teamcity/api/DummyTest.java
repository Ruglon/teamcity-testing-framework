package teamcity.api;

import io.restassured.RestAssured;
import org.testng.annotations.Test;
import teamcity.api.models.User;
import teamcity.api.spec.Specifications;

public class DummyTest extends BaseApiTest{

//    @Test
//    public void userShouldBeAbleGetAllProjects(){
//        RestAssured
//                .given()
//                .spec(Specifications.getSpec()
//                        .authSpec(User.builder()
//                                .username("admin").password("admin")
//                                .build()))
//                .get("/app/rest/projects");
//    }
}
