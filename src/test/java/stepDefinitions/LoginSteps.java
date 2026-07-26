package stepDefinitions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.qameta.allure.Description;
import io.qameta.allure.Step;

public class LoginSteps {
    
    WebDriver driver;

    @Given("User opens the HRM login page")
    @Step("Navigating to HRM Login Page")
    public void user_opens_the_hrm_login_page() {
        // Selenium Manager (bundled since Selenium 4.6) resolves a matching
        // chromedriver automatically - no webdriver.chrome.driver system
        // property needed here.
        ChromeOptions options = new ChromeOptions();

        // CI runners (e.g. GitHub Actions) have no display server, so Chrome
        // must run headless there. Local runs stay visible by default so
        // you can watch the browser while developing.
        boolean runningInCi = "true".equalsIgnoreCase(System.getenv("CI"));
        if (runningInCi) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1920,1080");
        }

        WebDriver standardDriver = new ChromeDriver(options);
        
        // এআই সেলফ-হিলিং সক্রিয় করার জন্য ড্রাইভারকে র‍্যাপ (Wrap) করা (Healenium কনসেপ্ট)
        // বাস্তব প্রজেক্টে: driver = SelfHealingDriver.create(standardDriver);
        this.driver = standardDriver; // সহজে বোঝার জন্য সরাসরি ব্যবহার করা হলো
        
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        if (!runningInCi) {
            driver.manage().window().maximize();
        }
    }

    @When("User enters valid username {string} and password {string}")
    @Step("Entering credentials: Username={0}")
    public void user_enters_valid_username_and_password(String username, String password) throws InterruptedException {
        Thread.sleep(3000); // পেজ লোডের জন্য সামান্য ওয়েট
        
        // ধরুন এআই যুগে ID বা Name বদলে গেছে, তাও AI এর ব্যাকএন্ড এটি খুঁজে নেবে
        WebElement usernameField = driver.findElement(By.name("username")); 
        usernameField.sendKeys(username);
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(password);
    }

    @When("User clicks on the login button")
    @Step("Clicking Login Button")
    public void user_clicks_on_the_login_button() {
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));
        loginBtn.click();
    }

    @Then("User should be redirected to the {string} page")
    @Description("Verify dashboard URL to confirm successful login")
    public void user_should_be_redirected_to_the_page(String expectedPage) throws InterruptedException {
        Thread.sleep(4000);
        String currentUrl = driver.getCurrentUrl();
        
        // ড্যাশবোর্ড ইউআরএল ভ্যালিডেশন (TestNG Assertion)
        Assert.assertTrue(currentUrl.contains("dashboard"), "Login Failed! Dashboard not loaded.");
    }

    // Runs after every scenario, pass or fail, so a failed assertion doesn't
    // leak an open Chrome process.
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}