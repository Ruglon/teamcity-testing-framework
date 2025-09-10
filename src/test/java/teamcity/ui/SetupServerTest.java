package teamcity.ui;

import org.testng.annotations.Test;
import teamcity.ui.setup.FirstStartPage;

public class SetupServerTest extends BaseUiTest{

    @Test(groups = {"Setup"})
    public void setupTeamCityServerTest(){
        FirstStartPage.open().setupFirstStart();
    }
}
