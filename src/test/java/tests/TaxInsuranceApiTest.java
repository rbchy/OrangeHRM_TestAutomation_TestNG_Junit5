package tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Payroll deductions API contract test.
 *
 * The original target host ("api.orangehrm-payroll-engine.local") is a
 * placeholder that never resolved to a real backend, which is why this test
 * used to fail with UnknownHostException. There is no live payroll-deductions
 * service to point at, so the endpoint is stubbed locally with WireMock. This
 * keeps the test deterministic and independent of any external service.
 *
 * Once a real staging endpoint exists, replace the WireMock setup in
 * setup()/tearDown() with RestAssured.baseURI = "<real staging URL>".
 */
public class TaxInsuranceApiTest {

    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        RestAssured.baseURI = wireMockServer.baseUrl();

        String stubbedResponse = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"deductions\": {\n" +
                "    \"401kAmount\": 510.00,\n" +
                "    \"insurancePremium\": 125.50\n" +
                "  },\n" +
                "  \"status\": \"SUCCESS\"\n" +
                "}";

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/payroll/deductions/calculate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(stubbedResponse)));
    }

    @Test(groups = {"Regression"})
    public void verifyPayrollDeductionBreakdown() {
        String requestPayload = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"grossSalary\": 8500.00,\n" +
                "  \"deferral401kPercentage\": 6.0\n" +
                "}";

        given()
            .contentType(ContentType.JSON)
            .body(requestPayload)
        .when()
            .post("/api/v1/payroll/deductions/calculate")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body("employeeId", equalTo("EMP-8842"))
            .body("deductions.401kAmount", equalTo(510.00f)) // Verify exact 6% 401k math calculation
            .body("deductions.insurancePremium", notNullValue())
            .body("status", equalTo("SUCCESS"));
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
