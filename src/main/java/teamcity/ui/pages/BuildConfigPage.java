package teamcity.ui.pages;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class BuildConfigPage extends BasePage{

    private SelenideElement runBtn = $(By.xpath("//button[@title='Run']"));
    private SelenideElement settingsBtn = $(By.xpath("//span[text()='Settings']/ancestor::span[contains(@class, 'EditEntity')]/parent::div"));
    private SelenideElement successBuild = $(By.xpath("//span[text()='Success']"));

    public static BuildConfigPage getCurrentPage() {
        return getPage(BuildConfigPage.class);
    }

    public void clickRunBuildBtn(){
        runBtn.click();
    }

    public void clickSettingsBtn(){
        settingsBtn.click();
    }

    public boolean checkThatBuildIsSuccessful(){
        successBuild.shouldBe(visible, BASE_WAITING);
        return successBuild.exists();
    }

    public void checkThatStepBuildCreated(String buildStep, String commandText){
        $x(String.format("//strong[contains(normalize-space(.), '%s')]", buildStep)).shouldBe(visible);
        $x(String.format("//div[contains(normalize-space(.), '%s')]", commandText)).shouldBe(visible);

    }



}
