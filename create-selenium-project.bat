@echo off
setlocal

REM ==== Настройки проекта ====
set GROUP_ID=com.rada.selenium
set ARTIFACT_ID=selenium-testng-project
set PACKAGE_DIR=com\rada\selenium
set MAIN_CLASS_NAME=GoogleSearchTest

REM ==== Создание проекта через Maven ====
echo [1/6] Генерация Maven-проекта...
mvn archetype:generate -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

cd %ARTIFACT_ID%

echo [2/6] Обновление pom.xml...
del pom.xml

(
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo   ^<modelVersion^>4.0.0^</modelVersion^>
echo   ^<groupId^>%GROUP_ID%^</groupId^>
echo   ^<artifactId^>%ARTIFACT_ID%^</artifactId^>
echo   ^<version^>1.0-SNAPSHOT^</version^>
echo
echo   ^<properties^>
echo     ^<maven.compiler.source^>17^</maven.compiler.source^>
echo     ^<maven.compiler.target^>17^</maven.compiler.target^>
echo   ^</properties^>
echo
echo   ^<dependencies^>
echo     ^<dependency^>
echo       ^<groupId^>org.seleniumhq.selenium^</groupId^>
echo       ^<artifactId^>selenium-java^</artifactId^>
echo       ^<version^>4.21.0^</version^>
echo     ^</dependency^>
echo     ^<dependency^>
echo       ^<groupId^>org.testng^</groupId^>
echo       ^<artifactId^>testng^</artifactId^>
echo       ^<version^>7.10.2^</version^>
echo       ^<scope^>test^</scope^>
echo     ^</dependency^>
echo     ^<dependency^>
echo       ^<groupId^>io.github.bonigarcia^</groupId^>
echo       ^<artifactId^>webdrivermanager^</artifactId^>
echo       ^<version^>5.8.0^</version^>
echo     ^</dependency^>
echo   ^</dependencies^>
echo
echo   ^<build^>
echo     ^<plugins^>
echo       ^<plugin^>
echo         ^<artifactId^>maven-compiler-plugin^</artifactId^>
echo         ^<version^>3.11.0^</version^>
echo         ^<configuration^>
echo           ^<release^>17^</release^>
echo         ^</configuration^>
echo       ^</plugin^>
echo       ^<plugin^>
echo         ^<artifactId^>maven-surefire-plugin^</artifactId^>
echo         ^<version^>3.2.5^</version^>
echo         ^<configuration^>
echo           ^<includes^>
echo             ^<include^>**/*Test.java^</include^>
echo           ^</includes^>
echo         ^</configuration^>
echo       ^</plugin^>
echo     ^</plugins^>
echo   ^</build^>
echo ^</project^>
) > pom.xml

echo [3/6] Удаление шаблонного теста...
del src\test\java\%PACKAGE_DIR%\AppTest.java

echo [4/6] Создание теста %MAIN_CLASS_NAME%.java...

mkdir src\test\java\%PACKAGE_DIR% 2>nul

(
echo package com.rada.selenium;
echo.
echo import io.github.bonigarcia.wdm.WebDriverManager;
echo import org.openqa.selenium.WebDriver;
echo import org.openqa.selenium.chrome.ChromeDriver;
echo import org.testng.annotations.AfterClass;
echo import org.testng.annotations.BeforeClass;
echo import org.testng.annotations.Test;
echo.
echo public class %MAIN_CLASS_NAME% {
echo.
echo     WebDriver driver;
echo.
echo     @BeforeClass
echo     public void setUp() {
echo         WebDriverManager.chromedriver().setup();
echo         driver = new ChromeDriver();
echo     }
echo.
echo     @Test
echo     public void openGoogleHomePage() {
echo         driver.get("https://www.google.com");
echo         System.out.println("Page title is: " + driver.getTitle());
echo     }
echo.
echo     @AfterClass
echo     public void tearDown() {
echo         if (driver != null) {
echo             driver.quit();
echo         }
echo     }
echo }
) > src\test\java\%PACKAGE_DIR%\%MAIN_CLASS_NAME%.java

echo [5/6] Сборка проекта...
mvn clean compile >nul

echo [6/6] Запуск теста...
mvn test

echo.
echo === Проект успешно создан и запущен! ===
pause
