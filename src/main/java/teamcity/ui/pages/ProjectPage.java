package teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import teamcity.ui.pages.admin.CreateBuildConfigFromURL;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class ProjectPage extends BasePage{

    private static final String PROJECT_URL = "/project/%s";

    public SelenideElement title = $("span[class*='ProjectPageHeader']");

    private static SelenideElement createInProjectBtn = $(By.xpath("//button[@data-hint-container-id='project-create-entity']"));
    private static SelenideElement newBuildConfiguration = $(By.xpath("//span[contains(text(), 'New build configuration')]/parent::div"));
    private static SelenideElement buildSteps = $(By.xpath("//span[contains(text(), 'Build Steps')]/ancestor::a[contains(@class, 'active--Gm')]"));


    public static ProjectPage open(String projectId) {
        return Selenide.open(PROJECT_URL.formatted(projectId), ProjectPage.class);
    }

    public static ProjectPage getCurrentPage() {
        return getPage(ProjectPage.class);
    }

    public static void addNewBuildConfiguration(){
        createInProjectBtn.shouldBe(Condition.visible, BASE_WAITING);
        createInProjectBtn.click();
        newBuildConfiguration.shouldBe(Condition.visible, BASE_WAITING);
        newBuildConfiguration.click();
    }

    public boolean buildStepsActive(){
        return buildSteps.exists();
    }


}
