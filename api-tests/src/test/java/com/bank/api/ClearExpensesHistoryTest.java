package com.bank.api;

import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;

public class ClearExpensesHistoryTest {
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";
    private final String EMAIL = "apiuser@test.com";
    private final String PASSWORD = "pass1234";

    private String getToken() {
        given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/register");
        Response res = given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}")
        .post("/login");
        return res.jsonPath().getString("token");
    }

    @Test
    public void clearAllExpensesHistory() {
        String token = getToken();
        // Получить все Id расходов
        Response getRes = given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses");
        List<String> ids = getRes.jsonPath().getList("id");
        // Удалить все расходы по Id
        for (String id : ids) {
            given().baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when().delete("/expenses/" + id)
            .then().statusCode(anyOf(is(200), is(204)));
        }
        // Проверить, что история пуста
        given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when().get("/expenses")
        .then().statusCode(200)
            .body("size()", is(0));
    }
}
