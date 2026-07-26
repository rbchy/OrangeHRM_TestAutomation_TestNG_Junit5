package tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

/**
 * End-to-End Testing: the full payroll chain for one pay cycle - time-clock
 * punch, manager approval, tax/deduction calculation, and the resulting
 * direct-deposit trigger - run as one continuous flow, checking that the
 * employee identity and pay amount carry through every hop unchanged.
 * Each hop is stubbed locally with WireMock, since there's no real payroll
 * backend deployment to run this against end to end.
 */
public class PayrollE2ETest {

    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        RestAssured.baseURI = wireMockServer.baseUrl();

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/timeclock/punch"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{ \"punchId\": \"PUNCH-1001\", \"employeeId\": \"EMP-8842\", \"status\": \"RECORDED\" }")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/timeclock/PUNCH-1001/approve"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{ \"punchId\": \"PUNCH-1001\", \"status\": \"APPROVED\", \"approvedHours\": 8.0 }")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/payroll/deductions/calculate"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{ \"employeeId\": \"EMP-8842\", \"grossPay\": 200.00, \"netPay\": 165.00, \"status\": \"SUCCESS\" }")));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/payroll/directdeposit/trigger"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{ \"employeeId\": \"EMP-8842\", \"depositId\": \"DEP-5001\", \"amount\": 165.00, \"status\": \"TRIGGERED\" }")));
    }

    @Test(groups = { "Regression" })
    public void fullPayCycleChainCompletesWithConsistentEmployeeAndAmount() {
        // 1) Employee clocks in/out.
        JsonPath punch = given()
                .contentType(ContentType.JSON)
                .body("{ \"employeeId\": \"EMP-8842\", \"hours\": 8.0 }")
            .when()
                .post("/api/v1/timeclock/punch")
            .then()
                .statusCode(200)
                .extract().jsonPath();
        String punchId = punch.getString("punchId");
        String employeeId = punch.getString("employeeId");

        // 2) Manager approves the punch.
        JsonPath approval = given()
            .when()
                .post("/api/v1/timeclock/" + punchId + "/approve")
            .then()
                .statusCode(200)
                .body("status", org.hamcrest.Matchers.equalTo("APPROVED"))
                .extract().jsonPath();
        double approvedHours = approval.getDouble("approvedHours");

        // 3) Tax/deduction engine calculates net pay.
        JsonPath deductions = given()
                .contentType(ContentType.JSON)
                .body(String.format("{ \"employeeId\": \"%s\", \"hoursWorked\": %s }", employeeId, approvedHours))
            .when()
                .post("/api/v1/payroll/deductions/calculate")
            .then()
                .statusCode(200)
                .body("status", org.hamcrest.Matchers.equalTo("SUCCESS"))
                .extract().jsonPath();
        double netPay = deductions.getDouble("netPay");

        // 4) Direct deposit is triggered for the calculated net pay.
        given()
                .contentType(ContentType.JSON)
                .body(String.format("{ \"employeeId\": \"%s\", \"amount\": %s }", employeeId, netPay))
            .when()
                .post("/api/v1/payroll/directdeposit/trigger")
            .then()
                .statusCode(200)
                .body("employeeId", org.hamcrest.Matchers.equalTo(employeeId))
                .body("status", org.hamcrest.Matchers.equalTo("TRIGGERED"))
                .body("amount", org.hamcrest.Matchers.equalTo((float) netPay));

        Assert.assertEquals(employeeId, "EMP-8842", "Employee identity changed somewhere in the chain");
        Assert.assertEquals(netPay, 165.00, 0.001, "Net pay changed unexpectedly between deduction and deposit");
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
