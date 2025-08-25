package teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class CreateBuildConfigurationPage  extends CreateBasePage {

    protected SelenideElement urlInput = $("#url");
    private static SelenideElement createInProjectBtn = $(By.xpath("//button[@data-hint-container-id='project-create-entity']"));
    private SelenideElement repositoryVerifiedMessage = $(By.xpath("//div[contains(@class, 'connectionSuccessful') and contains(., 'VCS repository has been verified')]"));
    private SelenideElement userNameBuildConfig = $(By.xpath("//input[@id='username']"));
    private SelenideElement passwordBuildConfig = $(By.xpath("//input[@id='password']"));

    //div[contains(@class, 'connectionSuccessful') and contains(text(), 'VCS repository has been verified')]
    //div[contains(@class, 'connectionSuccessful') and contains(., 'VCS repository has been verified')]
    //div[contains(@class, 'Subprojects')]//span[text()='%s']

    public static CreateBuildConfigurationPage getCurrentPage() {
        return page(CreateBuildConfigurationPage.class);
    }

    public void verifyParentProjectNameVisible(String projectName){
        SelenideElement element = $(String.format("*:text('%s')", projectName))
                .shouldBe(Condition.visible);
    }

    public CreateBuildConfigurationPage createFormBuild(String url) {
        baseCreateForm(url);
        return this;
    }

    public void setupBuildConfiguration(String userName, String userPassword) {
        userNameBuildConfig.val(userName);
        passwordBuildConfig.val(userPassword);
        submitButton.click();
    }

    public boolean repositorySuccessfullyVerified(){
        return repositoryVerifiedMessage.exists();
    }

//    public static void addNewBuildConfiguration(){
//        createInProjectBtn.shouldBe(Condition.visible, BASE_WAITING);
//        createInProjectBtn.click();
//        newBuildConfiguration.shouldBe(Condition.visible, BASE_WAITING);
//        newBuildConfiguration.click();
//    }
}
