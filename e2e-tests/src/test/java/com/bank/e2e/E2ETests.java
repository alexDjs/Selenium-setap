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

/**
 * End-to-end tests for MyBank web application.
 * How to run locally:
 * 1. Make sure Google Chrome is installed and chromedriver is available.
 * 2. Run: mvn -pl e2e-tests test
 * The browser will open in fullscreen mode for each test.
 */
public class E2ETests {
    private WebDriver driver;
    private final String BASE_URL = "https://mybank-8s6n.onrender.com";

    /**
     * After each test, close the browser with a short delay for stability.
     */
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                Thread.sleep(1000); // short delay before quit
            } catch (InterruptedException e) {
                // ignore
            }
            driver.quit();
        }
    }

    /**
     * Registers a new user and returns the authentication token.
     */
    private String registerAndGetToken(String email, String password) {
        RestAssured.given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .post("/register");
        Response loginRes = RestAssured.given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .post("/login");
        return loginRes.jsonPath().getString("token");
    }

    /**
     * Creates a new expense for the given user token.
     */
    private void createExpense(String token, String type, String amount) {
        RestAssured.given().baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"type\":\""+type+"\",\"amount\":\""+amount+"\",\"direction\":\"out\",\"location\":\"City\",\"product\":\"Prod\"}")
            .post("/expenses");
    }

    /**
     * Test: Successful login with valid credentials.
     * Verifies that the balance is displayed after login.
     */
    @Test
    public void loginShouldSucceed() {
        String userDataDir;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            userDataDir = "C:\\temp\\chrome-profile-" + UUID.randomUUID();
        } else {
            userDataDir = "/tmp/chrome-profile-" + UUID.randomUUID();
        }
        ChromeOptions options = new ChromeOptions();
        if (System.getenv("CI") != null) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--user-data-dir=" + userDataDir);
        driver = new ChromeDriver(options);
        driver.manage().window().fullscreen(); // open browser in fullscreen
        driver.get(BASE_URL + "/");
        driver.findElement(By.id("email")).sendKeys("admin@mybank.com");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.xpath("//button[text()='Login']")).click();
        assertTrue(driver.findElement(By.id("balance")).isDisplayed());
    }

    @Test
    public void logoutShouldShowLoginForm() {
        ChromeOptions options = new ChromeOptions();
        if (System.getenv("CI") != null) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
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
        ChromeOptions options = new ChromeOptions();
        if (System.getenv("CI") != null) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
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
        try {
            ChromeOptions options = new ChromeOptions();
            if (System.getenv("CI") != null) {
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
            driver.get(BASE_URL + "/");
            String title = driver.getTitle();
            System.out.println("Page title: " + title);
            assertEquals(title, "MyBank");
        } catch (Exception e) {
            System.out.println("Exception in pageShouldBeAccessible: " + e.getMessage());
            if (driver != null) {
                System.out.println("Page source: " + driver.getPageSource());
            }
            throw e;
        }
    }
}
