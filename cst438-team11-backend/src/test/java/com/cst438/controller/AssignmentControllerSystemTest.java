package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {

  public static final String CHROME_DRIVER_FILE_LOCATION =
      "C:/temp/chromeDriver/chromedriver.exe";

  public static final String URL = "http://localhost:3000";

  public static final int SLEEP_DURATION = 1000;

  WebDriver driver;

  @BeforeEach
  public void setUpDriver() throws Exception {
    System.setProperty(
        "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-all-origins=*");

        driver = new ChromeDriver(ops);

        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
  }

  @AfterEach
  public void terminateDriver() {
    if (driver != null) {
      driver.close();
      driver.quit();
      driver = null;
    }
  }

  //TODO: COMPLETE: Instructor adds new assignment successfully
  @Test
  public void systemTestAddAssignment() throws Exception {
    // specify year and semester for section search
    //

    driver.findElement(By.id("year")).sendKeys("2025");
    driver.findElement(By.id("semester")).sendKeys("Spring");
    driver.findElement(By.id("isectionlink")).click();
    Thread.sleep(SLEEP_DURATION);

    //TODO: consider searching by first section row
    String sectionElement = "a" + 8;

    driver.findElement(By.id(sectionElement)).click();
    Thread.sleep(SLEEP_DURATION);

    driver.findElement(By.id("addAssignment")).click();
    Thread.sleep(SLEEP_DURATION);

    driver.findElement(By.id("atitle")).sendKeys("Sample Assignment");
    driver.findElement(By.id("aduedate")).sendKeys("2025-04-10");
    driver.findElement(By.id("asave")).click();
    Thread.sleep(SLEEP_DURATION);

    String message = driver.findElement(By.id("amessage")).getText();

    assertEquals("assignment added", message);

    WebElement sampleAssignment = driver.findElement(By.xpath("//tr[td='Sample Assignment']"));
    List<WebElement> deleteButtons = sampleAssignment.findElements(By.tagName("button"));
    assertEquals(3, deleteButtons.size());
    deleteButtons.get(2).click();
    Thread.sleep(SLEEP_DURATION);

    List<WebElement> confirmButtons = driver
        .findElement(By.className("react-confirm-alert-button-group"))
        .findElements(By.tagName("button"));
    assertEquals(2,confirmButtons.size());
    confirmButtons.get(0).click();
    Thread.sleep(SLEEP_DURATION);

    assertThrows(NoSuchElementException.class, () ->
        driver.findElement(By.xpath("//tr[td='Sample Assignment']")));
  }
}
