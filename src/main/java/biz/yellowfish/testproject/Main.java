package biz.yellowfish.testproject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("webdriver.chrome.driver", "/Users/traveler/projects/chromedriver");

        WebDriver driver = new ChromeDriver();
        driver.get("http://www.nasdaq.com/");
        System.in.read();
    }
}
