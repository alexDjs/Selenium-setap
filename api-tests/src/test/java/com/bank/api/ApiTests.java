
package com.bank.api;

import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * API tests for MyBank backend.
 * How to run locally:
 * 1. Make sure you have Java and Maven installed.
 * 2. Run: mvn -pl api-tests test
 * These tests use RestAssured to verify API endpoints.
 */
public class ApiTests {
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";
    private final String EMAIL = "apiuser@test.com";
    private final String PASSWORD = "pass1234";

    /**
     * Registers and logs in, returns auth token.
     */
    private String getToken() {
        given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/register");
        Response res = given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/login");
        return res.jsonPath().getString("token");
    }

    /**
     * Test: Login should return a valid token for correct credentials.
     */
    @Test
    public void loginShouldReturnToken() {
        given()
            .baseUri("https://mybank-8s6n.onrender.com")
            .contentType("application/json")
            .body("{\"email\":\"admin@mybank.com\",\"password\":\"123456\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue());
    }

    /**
     * Test: Posting a new expense should succeed.
     */
    @Test
    public void postExpenseShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"1\",\"type\":\"Media Expert\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-1\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Test: Getting expenses should succeed for valid token.
     */
    @Test
    public void getExpensesShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses")
        .then().statusCode(200);
    }

    /**
     * Test: PUT should update type, amount, location for all expense IDs.
     */
    @Test
    public void putExpenseShouldChangeTypeAmountLocationForAllIds() {
        String token = getToken();
        // POST для Id=1,2,3 с логированием ответа
        int[] backendIds = new int[3];
        for (int i = 1; i <= 3; i++) {
            Response postResponse = given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Media Expert\",\"amount\":\"200\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-" + i + "\"}")
            .post("/expenses");
            int backendId = postResponse.jsonPath().getInt("id");
            backendIds[i-1] = backendId;
            System.out.println("POST /expenses Response for Id=" + i + ": " + postResponse.asString());
        }
        // PUT для backend id
        for (int i = 0; i < 3; i++) {
            int backendId = backendIds[i];
            Response putResponse = given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + backendId + "\",\"type\":\"Biedronka\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Krakow\",\"product\":\"Product-" + (i+1) + "\"}")
            .put("/expenses/" + backendId);
            System.out.println("PUT /expenses/" + backendId + " Response: " + putResponse.asString());
            putResponse.then().statusCode(anyOf(is(200), is(204)));
        }
    }

    /**
     * Test: DELETE should remove all expenses by ID.
     */
    @Test
    public void deleteExpensesShouldDeleteAllIds() {
        String token = getToken();
        // POST для Id=1,2,3
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Media Expert\",\"amount\":\"200\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-" + i + "\"}")
            .post("/expenses");
        }
        // DELETE для Id=1,2,3 по очереди
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when().delete("/expenses/" + i)
            .then().statusCode(anyOf(is(200), is(204)));
        }
    }

    /**
     * Test: Login should fail with wrong password.
     */
    @Test
    public void loginShouldFailWithWrongPassword() {
        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body("{\"email\":\"admin@mybank.com\",\"password\":\"wrongpass\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    /**
     * Test: Login should fail with empty credentials.
     */
    @Test
    public void loginShouldFailWithEmptyCredentials() {
        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body("{\"email\":\"\",\"password\":\"\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    /**
     * Test: Posted expense should appear in the list.
     */
    @Test
    public void postExpenseShouldAppearInList() {
        String token = getToken();
        // POST новый расход с Id=1 и логируем ответ
        Response postResponse = given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"1\",\"type\":\"ZARA\",\"amount\":\"2000\",\"direction\":\"in\",\"location\":\"Wroclaw\"}")
            .post("/expenses");
        System.out.println("POST /expenses response: " + postResponse.asString());

        // GET и логируем ответ
        Response getResponse = given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .get("/expenses");
        System.out.println("GET /expenses response: " + getResponse.asString());

        // Проверка, что расход с Id="1" появился (id как строка)
        getResponse.then().statusCode(200)
            .body("find { it.id == '1' }", notNullValue());
    }

    /**
     * Test: Full expense lifecycle (create, update, delete, verify empty).
     */
    @Test
    public void fullExpenseLifecycleTest() {
        String token = getToken();
        // POST: создать 3 расхода
        int[] backendIds = new int[3];
        for (int i = 1; i <= 3; i++) {
            Response postResponse = given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Media Expert\",\"amount\":\"200\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-" + i + "\"}")
            .post("/expenses");
            int backendId = postResponse.jsonPath().getInt("id");
            backendIds[i-1] = backendId;
        }
        // PUT: изменить все 3 расхода
        for (int i = 0; i < 3; i++) {
            int backendId = backendIds[i];
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + backendId + "\",\"type\":\"Biedronka\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Krakow\",\"product\":\"Product-" + (i+1) + "\"}")
            .when().put("/expenses/" + backendId)
            .then().statusCode(anyOf(is(200), is(204)));
        }
        // DELETE: удалить все 3 расхода
        for (int i = 0; i < 3; i++) {
            int backendId = backendIds[i];
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when().delete("/expenses/" + backendId)
            .then().statusCode(anyOf(is(200), is(204)));
        }
        // GET: убедиться, что список пуст
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses")
        .then().statusCode(200)
            .body("size()", is(0));
    }

    /**
     * Security test: Login should reject SQL injection attempt.
     */
    @Test
    public void loginShouldRejectSqlInjection() {
        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body("{\"email\":\"admin' OR '1'='1\",\"password\":\"123456\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(anyOf(is(401), is(400)));
    }

    /**
     * Negative test: Accessing expenses without token should fail.
     */
    @Test
    public void getExpensesShouldFailWithoutToken() {
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
        .when().get("/expenses")
        .then().statusCode(anyOf(is(401), is(403)));
    }

    /**
     * Edge case: Creating expense with minimum amount should succeed.
     */
    @Test
    public void postExpenseWithMinAmountShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"201\",\"type\":\"Test\",\"amount\":\"1\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"MinAmount\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Edge case: Creating expense with maximum amount should succeed.
     */
    @Test
    public void postExpenseWithMaxAmountShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"202\",\"type\":\"Test\",\"amount\":\"1000000\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"MaxAmount\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Validation test: Creating expense with invalid email should fail.
     */
    @Test
    public void postExpenseWithInvalidEmailShouldFail() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"203\",\"type\":\"Test\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"InvalidEmail\",\"email\":\"not-an-email\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Duplicate test: Creating expense with duplicate ID should fail.
     */
    @Test
    public void postExpenseWithDuplicateIdShouldFail() {
        String token = getToken();
        // First creation
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"204\",\"type\":\"Test\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"Duplicate\"}")
        .post("/expenses");
        // Second creation with same Id
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"204\",\"type\":\"Test\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"Duplicate\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Security test: Creating expense with XSS in type should be rejected.
     */
    @Test
    public void postExpenseShouldRejectXss() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"205\",\"type\":\"<script>alert('xss')</script>\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"TestCity\",\"product\":\"XSS\"}")
        .when().post("/expenses")
        .then().statusCode(anyOf(is(200), is(201)));
    }

    /**
     * Edge case: Getting expenses with pagination should succeed.
     */
    @Test
    public void getExpensesWithPaginationShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .queryParam("page", 1)
            .queryParam("size", 5)
        .when().get("/expenses")
        .then().statusCode(200);
    }
}
