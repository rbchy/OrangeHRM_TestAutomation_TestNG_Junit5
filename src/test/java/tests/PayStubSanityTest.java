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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Sanity Testing: after a targeted bug fix (e.g. "pay-stub download was
 * broken"), a quick, narrow check that just that one flow works - not a
 * full regression run. Stubbed locally with WireMock since there's no real
 * pay-stub backend to hit.
 */
public class PayStubSanityTest {

    private WireMockServer wireMockServer;

    @BeforeClass
    public void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        RestAssured.baseURI = wireMockServer.baseUrl();

        String response = "{\n" +
                "  \"employeeId\": \"EMP-8842\",\n" +
                "  \"payPeriod\": \"2026-07\",\n" +
                "  \"downloadUrl\": \"https://cdn.example.com/paystubs/EMP-8842-2026-07.pdf\",\n" +
                "  \"status\": \"READY\"\n" +
                "}";

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/payroll/paystub/EMP-8842/download"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }

    @Test(groups = { "Sanity" })
    public void verifyPayStubDownloadIsAvailable() {
        given()
        .when()
            .get("/api/v1/payroll/paystub/EMP-8842/download")
        .then()
            .statusCode(200)
            .body("employeeId", equalTo("EMP-8842"))
            .body("downloadUrl", notNullValue())
            .body("status", equalTo("READY"));
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
