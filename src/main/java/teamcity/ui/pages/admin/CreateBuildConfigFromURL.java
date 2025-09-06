package teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class CreateBuildConfigFromURL extends CreateBasePage {

    private SelenideElement buildConfigName = $("#buildTypeName");
    private SelenideElement defaultBranch = $("#branch");
    private SelenideElement proceedBtn = $(By.cssSelector("input[value='Proceed']"));
    private SelenideElement createDuplicateBtn = $(By.cssSelector("input[value='Create Duplicate VCS Root']"));

    public static CreateBuildConfigFromURL getCurrentPage() {
        return page(CreateBuildConfigFromURL.class);
    }

    public void checkRequiredFieldsNotEmpty(String buildConfigNameStr){
        buildConfigName.clear();
        buildConfigName.val(buildConfigNameStr);

        buildConfigName.shouldNotBe(Condition.empty);
        defaultBranch.shouldNotBe(Condition.empty);
    }

    public boolean checkIfDuplicate(){
        return createDuplicateBtn.exists();
    }

    public void clickCreateDuplicateBtn(){
        createDuplicateBtn.click();
    }

    public void clickProceedBtn(){
        proceedBtn.click();
    }
}
