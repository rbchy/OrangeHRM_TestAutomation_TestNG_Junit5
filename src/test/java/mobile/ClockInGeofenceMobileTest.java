package mobile;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * SCAFFOLD / TEMPLATE - Mobile Automation (Appium).
 *
 * There is no real employee mobile app (APK/IPA) or Appium server available
 * to this project, so this class is a compile-checked template only. It is
 * deliberately NOT registered in testng.xml, so it never runs as part of
 * `mvn test` and won't break the build.
 *
 * To make this runnable once a real app exists:
 *   1. Start an Appium server (default http://127.0.0.1:4723).
 *   2. Point APP_PATH at a real geofenced clock-in/out APK build.
 *   3. Swap the placeholder resource-id locators below for the app's real ones.
 *   4. Register this class in testng.xml under its own <test> block.
 *
 * Covers: employee mobile clock-in/out inside a geofenced work location,
 * and viewing a downloaded pay stub from the same app - the two mobile
 * scenarios called out in the test taxonomy.
 */
public class ClockInGeofenceMobileTest {

    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final String APP_PATH = "/path/to/hrm-mobile-app.apk"; // TODO: real APK path

    private AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Pixel_6_API_34");
        options.setApp(APP_PATH);
        // TODO: set real geofence mock location coordinates once the app/device is available, e.g.:
        // options.setCapability("appium:gpsEnabled", true);

        driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);
    }

    @Test(enabled = false) // TODO: enable once a real app + Appium server are wired up
    public void clockInWithinGeofence_recordsPunch() {
        WebElement clockInButton = driver.findElement(AppiumBy.id("com.hrm.mobile:id/btn_clock_in"));
        clockInButton.click();

        WebElement confirmation = driver.findElement(AppiumBy.id("com.hrm.mobile:id/txt_punch_status"));
        Assert.assertEquals(confirmation.getText(), "Clocked In");
    }

    @Test(enabled = false) // TODO: enable once a real app + Appium server are wired up
    public void clockOutOutsideGeofence_showsLocationError() {
        WebElement clockOutButton = driver.findElement(AppiumBy.id("com.hrm.mobile:id/btn_clock_out"));
        clockOutButton.click();

        WebElement errorMessage = driver.findElement(AppiumBy.id("com.hrm.mobile:id/txt_geofence_error"));
        Assert.assertEquals(errorMessage.getText(), "You must be at your assigned work location to clock out.");
    }

    @Test(enabled = false) // TODO: enable once a real app + Appium server are wired up
    public void viewPayStub_opensDownloadedDocument() {
        driver.findElement(AppiumBy.id("com.hrm.mobile:id/nav_paystub")).click();
        WebElement latestStub = driver.findElement(AppiumBy.id("com.hrm.mobile:id/list_item_paystub_latest"));
        latestStub.click();

        WebElement viewer = driver.findElement(AppiumBy.id("com.hrm.mobile:id/pdf_viewer"));
        Assert.assertTrue(viewer.isDisplayed());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
