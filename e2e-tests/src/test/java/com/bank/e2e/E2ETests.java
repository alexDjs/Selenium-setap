package com.bank.e2e;

import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.UUID;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.testng.Assert.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.UUID;

public class E2ETests {
    private WebDriver driver;
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                Thread.sleep(1000); // небольшая задержка перед quit
            } catch (InterruptedException e) {
                // ignore
            }
            driver.quit();
        }
    }

    private String registerAndGetToken(String email, String password) {
        RestAssured.given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .post("/register");
        Response loginRes = RestAssured.given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .post("/login");
        return loginRes.jsonPath().getString("token");
    }

    private void createExpense(String token, String type, String amount) {
        RestAssured.given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"type\":\""+type+"\",\"amount\":\""+amount+"\",\"direction\":\"out\",\"location\":\"City\",\"product\":\"Prod\"}")
            .post("/expenses");
    }

    @Test
    public void loginShouldSucceed() {
        String userDataDir;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            userDataDir = "C:\\temp\\chrome-profile-" + UUID.randomUUID();
        } else {
            userDataDir = "/tmp/chrome-profile-" + UUID.randomUUID();
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + userDataDir);
        driver = new ChromeDriver(options);
        driver.get(BASE_URL + "/");
        driver.findElement(By.id("email")).sendKeys("admin@mybank.com");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.xpath("//button[text()='Login']")).click();
        assertTrue(driver.findElement(By.id("balance")).isDisplayed());
    }

    @Test
    public void loginShouldFailWithWrongPassword() {
        String userDataDir;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            userDataDir = "C:\\temp\\chrome-profile-" + UUID.randomUUID();
        } else {
            userDataDir = "/tmp/chrome-profile-" + UUID.randomUUID();
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + userDataDir);
        driver = new ChromeDriver(options);
        driver.get(BASE_URL + "/");
        driver.findElement(By.id("email")).sendKeys("admin@mybank.com");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.xpath("//button[text()='Login']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-error")));
        String errorText = driver.findElement(By.id("login-error")).getText();
        assertTrue(errorText.contains("Invalid email or password"));
    }

    @Test
    public void logoutShouldShowLoginForm() {
        driver = new ChromeDriver();
        driver.get(BASE_URL + "/");
        driver.findElement(By.id("email")).sendKeys("admin@mybank.com");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.xpath("//button[text()='Login']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout-btn")));
        driver.findElement(By.id("logout-btn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-overlay")));
        String display = driver.findElement(By.id("auth-overlay")).getCssValue("display");
        assertTrue(display.equals("block") || display.equals("flex"));
    }

    @Test
    public void tableShouldBeVisibleAfterLogin() {
        String email = "e2euser+" + UUID.randomUUID() + "@test.com";
        String password = "pass1234";
        String token = registerAndGetToken(email, password);
        for (int i = 0; i < 3; i++) {
            createExpense(token, "E2E-Test", "10");
        }
        driver = new ChromeDriver();
        driver.get(BASE_URL + "/");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Login']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("table")));
        wait.until(d -> d.findElements(By.cssSelector("#table tbody tr")).size() >= 3);
        int rowCount = driver.findElements(By.cssSelector("#table tbody tr")).size();
        assertTrue(rowCount >= 3);
    }

    @Test
    public void pageShouldBeAccessible() {
        driver = new ChromeDriver();
        driver.get(BASE_URL + "/");
        assertEquals(driver.getTitle(), "MyBank");
    }
}
