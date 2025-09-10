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

public class UiTests {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Ошибка при закрытии браузера: " + e.getMessage());
            }
        }
    }

    // Login with wrong password -> expect login-error
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

    // Access dashboard without login (expect auth-overlay)
    @Test
    public void dashboardShouldNotBeAccessibleWithoutLogin() {
        driver.get("https://mybank-8s6n.onrender.com/");
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-overlay")));
        assertTrue(driver.findElement(By.id("auth-overlay")).isDisplayed());
    }

    // Responsive layout test (window resize)
    @Test
    public void layoutShouldBeResponsive() {
        driver.get("https://mybank-8s6n.onrender.com/");
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(400, 800));
        assertTrue(driver.findElement(By.id("login-btn")).isDisplayed());
    }

    // Invalid email format shows error (simplified)
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
