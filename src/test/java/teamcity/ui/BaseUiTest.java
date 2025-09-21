package teamcity.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import teamcity.BaseTest;
import teamcity.api.config.Config;
import teamcity.api.enums.Endpoint;
import teamcity.api.models.User;
import teamcity.ui.pages.LoginPage;

import java.util.Map;

public class BaseUiTest extends BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void setupUiTest() {
        // Get browser from system properties first, then fallback to config file
        String browser = System.getProperty("selenide.browser");
        if (browser == null || browser.isEmpty()) {
            browser = Config.getProperty("browser");
        }
        Configuration.browser = browser != null ? browser : "chrome";
        
        // Get base URL from system properties first, then fallback to config file
        String baseUrl = System.getProperty("selenide.baseUrl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            String host = Config.getProperty("host");
            baseUrl = host != null ? "http://" + host : "http://localhost:8111";
        }
        Configuration.baseUrl = baseUrl;
        
        // Get remote URL from system properties first, then fallback to config file
        String remote = System.getProperty("selenide.remote");
        if (remote == null || remote.isEmpty()) {
            remote = Config.getProperty("remote");
        }
        Configuration.remote = remote;
        
        // Get browser size from system properties first, then fallback to config file
        String browserSize = System.getProperty("selenide.browserSize");
        if (browserSize == null || browserSize.isEmpty()) {
            browserSize = Config.getProperty("browserSize");
        }
        Configuration.browserSize = browserSize != null ? browserSize : "1920x1080";

        // Debug logging
        System.out.println("Selenide Configuration:");
        System.out.println("  Browser: " + Configuration.browser);
        System.out.println("  Base URL: " + Configuration.baseUrl);
        System.out.println("  Remote: " + Configuration.remote);
        System.out.println("  Browser Size: " + Configuration.browserSize);

        // Ensure browser is not null
        if (Configuration.browser == null || Configuration.browser.isEmpty()) {
            System.err.println("ERROR: Browser configuration is null or empty!");
            Configuration.browser = "chrome"; // Fallback
        }

        // Set browser capabilities before any browser operations
        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
        
        // Set additional Chrome capabilities for better compatibility
        if ("chrome".equals(Configuration.browser)) {
            Configuration.browserCapabilities.setCapability("browserName", "chrome");
            Configuration.browserCapabilities.setCapability("browserVersion", "latest");
        }

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true)
                .includeSelenideSteps(true));
    }

    @AfterMethod(alwaysRun = true)
    public void closeWebDriver() {
        Selenide.closeWebDriver();
    }

    protected void loginAs(User user) {
        superUserCheckedRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        LoginPage.open().login(testData.getUser());
    }
}
