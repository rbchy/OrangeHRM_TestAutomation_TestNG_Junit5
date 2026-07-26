package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.time.Duration;

/**
 * NOTE: This test targets "https://orangehrm-payroll.local/time/logSheets",
 * a placeholder host with no real backend behind it (time-clock features
 * aren't part of the public OrangeHRM demo). It's tagged "Flaky" so
 * testng.xml's group filter excludes it from `mvn test` until it's pointed
 * at a real staging environment (swap the URL in testInvalidWorkHoursValidation
 * below) or rebuilt against a local fixture page. Untag it once that's done.
 */
public class TimeClockNegativeTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Test(groups = {"Negative", "Flaky"})
    public void testInvalidWorkHoursValidation() {
        driver.get("https://orangehrm-payroll.local/time/logSheets");
        
        // Input impossible log metric (26 hours)
        By hourInput = By.id("time_sheet_daily_hours");
        wait.until(ExpectedConditions.visibilityOfElementLocated(hourInput)).clear();
        driver.findElement(hourInput).sendKeys("26");
        
        driver.findElement(By.id("btnSubmitTime")).click();
        
        // Assert native UI boundaries capture the constraint error
        By validationAlert = By.className("validation-error-message");
        String actualErrorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(validationAlert)).getText();
        
        Assert.assertEquals(actualErrorMessage, "Daily logged working hours cannot exceed 24 hours.", 
            "The system failed to block an impossible 26-hour daily time entry input.");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}