package biz.yellowfish.selenium.tmc;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import static org.testng.Assert.*;

public class MiscSeleniumTest {
    public static final String TMC_URL = "http://www.tmcbonds.com";

    private WebDriver driver;
    @BeforeClass
    public void beforeClass() {
        System.setProperty("webdriver.chrome.driver", "/Users/traveler/projects/chromedriver");
        this.driver =  new ChromeDriver(); // new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
    }

    @AfterClass
    public void  afterClass() {
        this.driver.close();
    }

    @Test
    public void hasLoginButton() throws InterruptedException {
        this.driver.get(TMC_URL);
        Thread.sleep(5*1000L);
        WebElement button = this.driver.findElement(By.id("login_opener"));
        WebElement span = button.findElement(By.className("ui-button-text"));
        String spantext = span.getText();
        assertNotNull(spantext);
        assertEquals(spantext, "LOGIN");
        WebElement lock = button.findElement(By.className("ui-icon-locked"));
        String lockCss = lock.getCssValue("background-image");
        assertNotNull(lockCss);
        assertTrue(lockCss.startsWith("url("));

        String url = lockCss.replaceAll("^url\\(\"","").replaceAll("\"\\)$","").trim();
        assertTrue(url.length() > 0);
        BufferedImage image = this.fetchImage(url);
        assertNotNull(image);
        assertEquals(image.getHeight(), 240);
        assertEquals(image.getWidth(), 256);
    }

    @Test(dependsOnMethods = {"hasLoginButton"}, enabled = true)
    public void loginButtonBringsUpDialog() {
        this.driver.get(TMC_URL);
        WebElement button = this.driver.findElement(By.id("login_opener"));
        button.click();
        WebDriverWait wait = new WebDriverWait(driver, 2);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ui-id-1")));
        WebElement uiid = this.driver.findElement(By.id("ui-id-1"));
        WebElement dialogDiv = uiid.findElement(By.xpath("../.."));
        String widthString = dialogDiv.getCssValue("width");
        assertNotNull(widthString);
        double parentWidth = Double.parseDouble(widthString.replaceAll("px$", ""));
        assertTrue((300.0 - parentWidth) <= 10.0);
        String position = dialogDiv.getCssValue("position");
        assertTrue(position.equals("absolute") || position.equals("relative"));

        WebElement username = this.driver.findElement(By.id("login"));
        double usernameWidth = Double.parseDouble(username.getCssValue("width").trim().replaceAll("px$",""));
        assertTrue(usernameWidth >= 0.8*parentWidth);

        WebElement password = this.driver.findElement(By.id("password"));
        assertEquals(password.getCssValue("width"), username.getCssValue("width"));

        WebElement form = username.findElement(By.xpath("../.."));
        assertEquals(form.getAttribute("method").toLowerCase(), "post");
        assertEquals(form.getAttribute("action"), "https://www.tmcbonds.com/login/");
    }

    @Test(enabled = false)
    public void newsAndEventsOnHomePageIsNotEmpty() {
        this.navigateToIFrame("mybcontainer_iframe", By.id("smoothmenu1"));
        List<WebElement> contentHeaders = this.driver.findElements(By.className("welcome-content-header"));
        WebElement contentHeader = contentHeaders.get(1);
        assertEquals(contentHeader.getText(), "News & Events");
        WebElement container = this.driver.findElement(By.id("news-container"));
        WebElement ul = container.findElement(By.tagName("ul"));
        List<WebElement> list = ul.findElements(By.tagName("li"));
        assertNotNull(list);
        assertTrue(list.size() > 0);
        String text = list.get(0).findElement(By.tagName("span")).getText();
        assertTrue(text.length() > 0);
    }

    @Test(enabled = false)
    public void testDownloadFile() {
        this.navigateToIFrame("mybcontainer_iframe", By.id("smoothmenu1"));
        WebElement newsLink = this.driver.findElement(By.cssSelector("div#smoothmenu1 > ul > li:nth-child(5) > ul > li:first-child > a"));
        newsLink.click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iframe_content")));
        this.navigateToIFrame("iframe_content", By.className("title-text"));
    }

    private void navigateToIFrame(String iframeId, By anchorElement) {
        this.driver.get(TMC_URL);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        By frameBy = By.id(iframeId);
        wait.until(ExpectedConditions.presenceOfElementLocated(frameBy));
        WebElement frame = this.driver.findElement(frameBy);
        String href = frame.getAttribute("src");
        System.out.println("IFRAME URL: "+ href);
        this.driver.get(href);
        wait.until(ExpectedConditions.presenceOfElementLocated(anchorElement));
    }


    private BufferedImage fetchImage(String urlString) {
        try {
            urlString = urlString.trim().replaceAll("^url\\(","").replaceAll("\\)$","");
            URL url = new URL(urlString);
            return ImageIO.read(url);

        } catch (Exception e) {
            return null;
        }
    }
}
