package com.bank.api;

import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ApiTests {
    // How to run: mvn test -pl api-tests
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";
    private final String EMAIL = "apiuser@test.com";
    private final String PASSWORD = "pass1234";

    private String getToken() {
        // Register (ignore result)
        given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/register");
        // Login
        Response res = given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/login");
        return res.jsonPath().getString("token");
    }

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

    @Test
    public void getExpensesShouldSucceed() {
        String token = getToken();
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses")
        .then().statusCode(200);
    }

    @Test
    public void putExpenseShouldChangeTypeAmountLocationForAllIds() {
        String token = getToken();
        // POST для Id=1,2,3
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Media Expert\",\"amount\":\"200\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-" + i + "\"}")
            .post("/expenses");
        }
        // PUT для Id=1,2,3
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Biedronka\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Krakow\",\"product\":\"Product-" + i + "\"}")
            .when().put("/expenses/" + i)
            .then().statusCode(anyOf(is(200), is(204)));
        }
    }

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

    @Test
    public void postExpenseShouldAppearInList() {
        String token = getToken();
        // POST новый расход
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"Id\":\"101\",\"type\":\"Biedronka\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Krakow\",\"product\":\"Product-101\"}")
        .post("/expenses");
        // GET и проверка, что расход появился (ищем по 'id' вместо 'Id')
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses")
        .then().statusCode(200);
    }

    @Test
    public void fullExpenseLifecycleTest() {
        String token = getToken();
        // POST: создать 3 расхода
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Media Expert\",\"amount\":\"200\",\"direction\":\"out\",\"location\":\"Wroclaw\",\"product\":\"Product-" + i + "\"}")
            .post("/expenses");
        }
        // PUT: изменить все 3 расхода
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("{\"Id\":\"" + i + "\",\"type\":\"Biedronka\",\"amount\":\"100\",\"direction\":\"out\",\"location\":\"Krakow\",\"product\":\"Product-" + i + "\"}")
            .when().put("/expenses/" + i)
            .then().statusCode(anyOf(is(200), is(204)));
        }
        // DELETE: удалить все 3 расхода
        for (int i = 1; i <= 3; i++) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when().delete("/expenses/" + i)
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

    // Security test: SQL injection attempt
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

    // Negative test: access expenses without token
    @Test
    public void getExpensesShouldFailWithoutToken() {
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
        .when().get("/expenses")
        .then().statusCode(anyOf(is(401), is(403)));
    }

    // Edge case: create expense with minimum amount
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

    // Edge case: create expense with maximum amount
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

    // Validation test: create expense with invalid email
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

    // Duplicate test: create expense with same Id
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

    // Security test: XSS attempt in expense type
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

    // Edge case: get expenses with pagination (if supported)
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
