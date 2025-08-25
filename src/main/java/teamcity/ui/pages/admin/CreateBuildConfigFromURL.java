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

    public static CreateBuildConfigFromURL getCurrentPage() {
        return page(CreateBuildConfigFromURL.class);
    }

    public void checkRequiredFieldsNotEmpty(){
        buildConfigName.shouldNotBe(Condition.empty);
        defaultBranch.shouldNotBe(Condition.empty);
    }

    public void clickProceedBtn(){
        proceedBtn.click();
    }
}
