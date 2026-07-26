package tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;

/**
 * API Testing: pay-stub and insurance-benefits backend endpoints checked
 * for both response code (200 OK) and JSON schema conformance. Stubbed
 * locally with WireMock; the schemas live under
 * src/test/resources/schemas/.
 */
public class InsuranceApiTest {

    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        RestAssured.baseURI = wireMockServer.baseUrl();

        String insuranceResponse = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"planTier\": \"FAMILY\",\n" +
                "  \"dependents\": 2,\n" +
                "  \"monthlyPremium\": 400.75,\n" +
                "  \"status\": \"ACTIVE\"\n" +
                "}";

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/benefits/insurance/EMP-8842"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(insuranceResponse)));

        String paystubResponse = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"payPeriod\": \"2026-07\",\n" +
                "  \"downloadUrl\": \"https://cdn.example.com/paystubs/EMP-8842-2026-07.pdf\",\n" +
                "  \"status\": \"READY\"\n" +
                "}";

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/payroll/paystub/EMP-8842/download"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(paystubResponse)));
    }

    @Test(groups = { "Regression" })
    public void verifyInsuranceBenefitsSchema() {
        given()
        .when()
            .get("/api/v1/benefits/insurance/EMP-8842")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body(matchesJsonSchemaInClasspath("schemas/insurance-benefits-schema.json"));
    }

    @Test(groups = { "Regression" })
    public void verifyPayStubSchema() {
        given()
        .when()
            .get("/api/v1/payroll/paystub/EMP-8842/download")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body(matchesJsonSchemaInClasspath("schemas/paystub-schema.json"));
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
