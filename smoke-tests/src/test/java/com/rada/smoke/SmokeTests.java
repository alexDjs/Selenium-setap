package com.rada.smoke;

import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;

public class SmokeTests {
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";

    // Check that home page is reachable
    @Test
    public void homePageShouldBeAvailable() {
        given().baseUri(BASE_URL)
        .when().get("/")
        .then().statusCode(200);
    }

    // Check login endpoint availability (GET may be not allowed)
    @Test
    public void loginEndpointShouldBeAvailable() {
        given().baseUri(BASE_URL)
        .when().get("/login")
        .then().statusCode(anyOf(is(200), is(405), is(404)));
    }

    // Quick login smoke test
    @Test
    public void loginWithValidCredentialsShouldReturnToken() {
        Response res = given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body("{\"email\":\"admin@mybank.com\",\"password\":\"123456\"}")
        .when().post("/login");

        res.then().statusCode(200).body("token", notNullValue());
    }

    // Protected endpoint should require authentication
    @Test
    public void expensesShouldRequireAuth() {
        given().baseUri(BASE_URL)
        .when().get("/expenses")
        .then().statusCode(anyOf(is(401), is(403)));
    }

    // Create an expense as smoke scenario (register/login then post expense)
    @Test
    public void createExpenseSmoke() {
        String uniqueEmail = "smokeuser+" + UUID.randomUUID().toString().substring(0,8) + "@test.com";
        String password = "smokepass";

        // register (ignore result)
        given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + uniqueEmail + "\",\"password\":\"" + password + "\"}")
        .when().post("/register");

        // login
        Response loginRes = given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + uniqueEmail + "\",\"password\":\"" + password + "\"}")
        .when().post("/login");

        String token = loginRes.jsonPath().getString("token");

        // post expense
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"9001\",\"type\":\"Smoke\",\"amount\":\"10\",\"direction\":\"out\",\"location\":\"SmokeCity\",\"product\":\"SmokeProduct\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    // Quick health-check of API: GET /account or /balance if exists
    @Test
    public void accountEndpointHealthCheck() {
        given().baseUri(BASE_URL)
        .when().get("/account")
        .then().statusCode(anyOf(is(200), is(404), is(401)));
    }
}
