package biz.yellowfish.selenium.tmc;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class MiscSeleniumTest {
    public static final String TMC_URL = "http://www.tmcbonds.com";

    private WebDriver driver;
    @BeforeClass
    public void beforeClass() {
        System.setProperty("webdriver.chrome.driver", "./lib/chromedriver.linux");
        Map<String,Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", "./downloads");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        this.driver =  new ChromeDriver(capabilities); // new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
    }

    @AfterClass
    public void  afterClass() {
        this.driver.close();
    }

    @Test(enabled = false)
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

    @Test(dependsOnMethods = {"hasLoginButton"}, enabled = false)
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
    }

    @Test(enabled = true)
    public void testDownloadFile() throws Exception {
        this.navigateToIFrame("mybcontainer_iframe", By.id("smoothmenu1"));
        WebElement newsCommentaryLink = this.driver.findElement(By.cssSelector("div#smoothmenu1 > ul > li:nth-child(5) > a"));
        Actions actions = new Actions(this.driver);
        actions.moveToElement(newsCommentaryLink);
        actions.build().perform();

        WebElement newsLink = newsCommentaryLink.findElement(By.xpath("../ul/li[1]/a"));
        newsLink.click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iframe_content")));
        this.sleep(5);
        WebElement frame = this.driver.findElement(By.id("iframe_content"));
        this.driver.get(frame.getAttribute("src"));
        this.sleep(5);
        wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title-text")));
        List<WebElement> downloadLinks = driver.findElements(By.linkText("Click Here"));
        assertTrue(downloadLinks.size() > 0);
        WebElement downloadLink = downloadLinks.get(1);
        assertNotNull(downloadLink);
        Path downloads = Paths.get(".", "downloads");
        this.deleteAllFilesInDirectory(downloads);
        downloadLink.click();
        this.sleep(5);
        // check if file is present
        Path pdf = this.findFirstPdf(downloads);
        assertNotNull(pdf);
        assertTrue(Files.size(pdf) > 0);
    }

    private Path findFirstPdf(Path directory) throws Exception {
        final AtomicReference<Path> pdf = new AtomicReference<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".pdf")) {
                    pdf.set(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return pdf.get();
    }

    private void deleteAllFilesInDirectory(Path directory) throws Exception {
        final Set<Path> toDelete = new HashSet<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(file) && !file.getFileName().toString().endsWith("do-not-delete")) {
                    toDelete.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        for (Path del : toDelete) {
            Files.delete(del);
        }
    }

    private void navigateToIFrame(String iframeId, By anchorElement) {
        this.driver.get(TMC_URL);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        By frameBy = By.id(iframeId);
        wait.until(ExpectedConditions.presenceOfElementLocated(frameBy));
        this.sleep(5);
        WebElement frame = this.driver.findElement(frameBy);
        String href = frame.getAttribute("src");
        System.out.println("IFRAME URL: "+ href);
        this.driver.get(href);
        wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(anchorElement));
    }

    private void sleep(int sec) {
        try {
            Thread.sleep(sec*1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
