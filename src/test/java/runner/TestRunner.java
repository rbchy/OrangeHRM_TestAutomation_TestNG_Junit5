package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepDefinitions"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports.html",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" // অলুর রিপোর্ট প্লাগইন
    },
    tags = "@Smoke"
)
public class TestRunner extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false) // সহজে সিকুয়েন্সিয়ালি রান করার জন্য false রাখা হয়েছে
    public Object[][] scenarios() {
        return super.scenarios();
    }
}