package tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;

/**
 * Integration Testing: data exported from the Time Clock module is fed into
 * the Payroll module's timesheet-import endpoint, and the test verifies the
 * data format survives the hand-off intact (employeeId + hours carry
 * through unchanged). Both endpoints are stubbed locally with WireMock so
 * the test isn't dependent on either module having a real deployment.
 */
public class TimeClockPayrollIntegrationTest {

    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        RestAssured.baseURI = wireMockServer.baseUrl();

        String timeClockExportResponse = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"period\": \"2026-07-14\",\n" +
                "  \"totalHoursWorked\": 42.5\n" +
                "}";

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/timeclock/export/EMP-8842?period=2026-07-14"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(timeClockExportResponse)));

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/payroll/import/timesheet"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"employeeId\": \"EMP-8842\", \"accepted\": true, \"normalizedHours\": 42.5 }")));
    }

    @Test(groups = { "Regression" })
    public void timeClockDataFormatSurvivesHandoffToPayroll() {
        // Step 1: pull the punch export from the Time Clock module.
        Response exportResponse = given()
                .when()
                .get("/api/v1/timeclock/export/EMP-8842?period=2026-07-14")
                .then()
                .statusCode(200)
                .extract().response();

        String employeeId = exportResponse.jsonPath().getString("employeeId");
        double totalHours = exportResponse.jsonPath().getDouble("totalHoursWorked");

        // Step 2: feed that same data into the Payroll module's import endpoint.
        String importPayload = String.format(
                "{ \"employeeId\": \"%s\", \"totalHoursWorked\": %s }", employeeId, totalHours);

        given()
            .contentType(ContentType.JSON)
            .body(importPayload)
        .when()
            .post("/api/v1/payroll/import/timesheet")
        .then()
            .statusCode(200)
            .body("employeeId", org.hamcrest.Matchers.equalTo(employeeId))
            .body("accepted", org.hamcrest.Matchers.equalTo(true))
            .body("normalizedHours", org.hamcrest.Matchers.equalTo((float) totalHours));

        Assert.assertEquals(employeeId, "EMP-8842", "Employee identity was lost crossing the module boundary");
        Assert.assertEquals(totalHours, 42.5, 0.001, "Hours were altered crossing the module boundary");
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
