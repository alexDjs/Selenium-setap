package com.bank.ui;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.testng.Assert.assertTrue;

/**
 * UI tests for MyBank web application.
 * How to run locally:
 * 1. Make sure Google Chrome is installed and chromedriver is available.
 * 2. Run: mvn -pl ui-tests test
 * The browser will open in fullscreen mode for each test.
 */
public class UiTests {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();
        if (System.getenv("CI") != null) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        // Open browser in fullscreen (headless: set large window size)
        if (System.getenv("CI") != null) {
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        } else {
            driver.manage().window().fullscreen();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Error closing browser: " + e.getMessage());
            }
        }
    }

    /**
     * Test: Login with wrong password should show login error.
     */
    @Test
    public void loginShouldFailWithWrongPassword() {
        driver.get("https://mybank-8s6n.onrender.com/");
        driver.findElement(By.id("email")).sendKeys("admin@mybank.com");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("login-btn")).click();
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-error")));
        String errorText = driver.findElement(By.id("login-error")).getText();
        assertTrue(errorText != null && (errorText.startsWith("Login error:") || errorText.toLowerCase().contains("invalid")));
    }

    /**
     * Test: Dashboard should not be accessible without login (auth overlay).
     */
    @Test
    public void dashboardShouldNotBeAccessibleWithoutLogin() {
        driver.get("https://mybank-8s6n.onrender.com/");
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-overlay")));
        assertTrue(driver.findElement(By.id("auth-overlay")).isDisplayed());
    }

    /**
     * Test: Responsive layout should display login button after window resize.
     */
    @Test
    public void layoutShouldBeResponsive() {
        driver.get("https://mybank-8s6n.onrender.com/");
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(400, 800));
        assertTrue(driver.findElement(By.id("login-btn")).isDisplayed());
    }

    /**
     * Test: Invalid email format should show login error.
     */
    @Test
    public void loginShouldShowErrorForInvalidEmailFormat() {
        driver.get("https://mybank-8s6n.onrender.com/");
        driver.findElement(By.id("email")).sendKeys("not-an-email");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.id("login-btn")).click();

        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-error")));
        assertTrue(driver.findElement(By.id("login-error")).isDisplayed());
    }

}
