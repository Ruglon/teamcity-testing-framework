package teamcity;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;
import teamcity.api.requests.CheckedRequests;
import teamcity.api.spec.Specifications;

public class BaseTest {
    protected SoftAssert softy;
    protected CheckedRequests superUserCheckedRequests = new CheckedRequests(Specifications.superUserAuth());

    @BeforeMethod(alwaysRun = true)
    public void beforeTest() {
        softy = new SoftAssert();
    }

    @AfterMethod(alwaysRun = true)
    public void afterTest() {
        softy.assertAll();
    }
}
