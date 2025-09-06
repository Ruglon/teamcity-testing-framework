package teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class EditBuildPage extends BasePage{

    private static final String EDIT_BUILD_PAGE = "/admin/editBuildRunners.html?id=buildType%%3A%s";

    private SelenideElement buildTypes = $(By.xpath("//span[text()='Build Steps']/ancestor::div[@class='ring-tabs-container']"));
    private SelenideElement addBuildStep = $(By.xpath("//span[text()='Add build step']/parent::a"));

    /**
     * New Build Steps
     * @return
     */
    private SelenideElement searchBuildStep = $(By.xpath("//input[@type='search']"));
    private SelenideElement addStepCommandLine = $(By.xpath("//span[text()='Command Line']/parent::div"));
    private SelenideElement stepNameInput = $(By.xpath("//input[@id='buildStepName']"));
    private SelenideElement stepIdInput = $(By.xpath("//input[@id='newRunnerId']"));
    private SelenideElement inputCodeMirror = $(byClassName("CodeMirror-scroll"));
    private SelenideElement inputCodeMirrorLine = $(By.xpath("//div[@class='CodeMirror-lines']"));
    private SelenideElement customSriptInput = $(By.xpath("//div[@class='CodeMirror-code']//pre//span"));
    private SelenideElement saveBuildStepBtn = $(By.xpath("//input[@value='Save']"));


    public static EditBuildPage openEditBuildRunners(String buildId) {
        String dynamicUrl = EDIT_BUILD_PAGE.formatted(buildId);
        System.out.println("Generated URL: " + Configuration.baseUrl + dynamicUrl);  // Debug
        return Selenide.open(dynamicUrl, EditBuildPage.class);
    }

    public static BuildConfigPage getCurrentPage() {
        return getPage(BuildConfigPage.class);
    }

    public void createNewBuildSteps(String stepName, String stepId, String commandText){
        buildTypes.click();
        addBuildStep.shouldBe(Condition.visible, BASE_WAITING);
        addBuildStep.click();

        addStepCommandLine.shouldBe(Condition.visible, BASE_WAITING);
        addStepCommandLine.click();

        stepNameInput.shouldBe(Condition.visible, BASE_WAITING);
        stepNameInput.val(stepName);
        stepIdInput.val(stepId);

        executeJavaScript(
                "arguments[0].parentNode.CodeMirror.setValue(arguments[1]);" +
                        "arguments[0].parentNode.CodeMirror.refresh();",
                inputCodeMirror, commandText
        );

        saveBuildStepBtn.click();
    }
}
