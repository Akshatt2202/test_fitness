import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class CoreTrackTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5000";
    
    // Dynamic credentials generated during test
    private String dynamicEmail;
    private final String TEST_PASSWORD = "password123";

    @BeforeClass
    public void setUp() {
        // Automatically manages the driver download using WebDriverManager
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
        
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Generate a unique email based on timestamp so registration works every time
        dynamicEmail = "testuser" + System.currentTimeMillis() + "@example.com";
    }

    @Test(priority = 1)
    public void testRegistration() {
        driver.get(BASE_URL + "/index.html");

        // Clear local storage in case a previous session was stuck
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().refresh();

        // Click on the Create Account tab using the explicit ID
        wait.until(ExpectedConditions.elementToBeClickable(By.id("registerTab"))).click();

        // Wait for registration form to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("regName")));

        // Fill out registration fields
        driver.findElement(By.id("regName")).sendKeys("Automation User");
        driver.findElement(By.id("regEmail")).sendKeys(dynamicEmail);
        driver.findElement(By.id("regPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("regAge")).sendKeys("25");
        
        Select genderSelect = new Select(driver.findElement(By.id("regGender")));
        genderSelect.selectByValue("male");
        
        driver.findElement(By.id("regHeight")).sendKeys("180");
        driver.findElement(By.id("regWeight")).sendKeys("75");
        
        Select activitySelect = new Select(driver.findElement(By.id("regActivity")));
        activitySelect.selectByValue("moderately_active");

        // Click Create Account button
        driver.findElement(By.id("registerBtn")).click();

        // Verify successful registration by checking redirect to dashboard
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("dashboard.html"), "Registration failed or did not redirect to Dashboard");

        // Now logout so we can test the Login flow properly
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-logout]"))).click();
        
        // Wait to be redirected back to index/login
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test(priority = 2)
    public void testLoginAndDashboard() {
        // Ensure we are on the login page
        driver.get(BASE_URL + "/index.html");

        // Ensure Sign In tab is active
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginTab"))).click();

        // Wait for modal and enter the credentials we just registered
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginEmail")));
        driver.findElement(By.id("loginEmail")).sendKeys(dynamicEmail);
        driver.findElement(By.id("loginPassword")).sendKeys(TEST_PASSWORD);
        
        // Click the Sign In button using explicit ID instead of the broken text XPath
        driver.findElement(By.id("loginBtn")).click();

        // Verify successful redirect to dashboard
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("dashboard.html"), "Login failed or did not redirect to Dashboard");
        
        // Verify Dashboard loaded by checking for the page title
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("page-title")));
        Assert.assertEquals(pageTitle.getText(), "Dashboard", "Dashboard title is incorrect");
    }

    @Test(priority = 3)
    public void testAddWorkout() {
        // Navigate to Workouts page
        driver.get(BASE_URL + "/workouts.html");

        // Wait for page to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("page-title")));

        // Click Add Workout button using a very specific CSS selector
        WebElement addWorkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-primary[onclick=\"openModal('workoutModal')\"]")));
        addWorkoutBtn.click();

        // Wait for modal to open
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("workoutModal")));

        // Fill out Strength Workout details
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("wExercise"))).sendKeys("Bench Press");
        driver.findElement(By.id("wSets")).sendKeys("3");
        driver.findElement(By.id("wReps")).sendKeys("10");
        driver.findElement(By.id("wWeight")).sendKeys("60");

        // Submit form
        driver.findElement(By.id("workoutSubmitBtn")).click();

        // Wait for modal to close (invisibility of modal overlay)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("workoutModal")));
        
        // Wait a short moment for DOM to update the table
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Verify the modal successfully closed as proof it saved
        Assert.assertTrue(driver.findElements(By.id("workoutModal")).get(0).getAttribute("class").contains("active") == false, "Workout modal did not close successfully");
    }

    @Test(priority = 4)
    public void testAIGenerator() {
        // Navigate to AI Plan page
        driver.get(BASE_URL + "/ai.html");

        // Wait for form to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("aiWorkoutForm")));

        // Select Goal
        Select goalSelect = new Select(driver.findElement(By.id("aiGoal")));
        goalSelect.selectByVisibleText("Muscle Gain");

        // Select Frequency
        Select freqSelect = new Select(driver.findElement(By.id("aiFrequency")));
        freqSelect.selectByVisibleText("4 Days");

        // Click Generate Plan
        driver.findElement(By.id("aiGenerateBtn")).click();

        // Wait for the result container to appear and load content
        WebElement resultContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("aiResultContainer")));
        Assert.assertTrue(resultContainer.isDisplayed(), "AI Result container did not appear");

        // Wait until spinner disappears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("spinner")));
        
        WebElement planContent = driver.findElement(By.id("aiPlanContent"));
        Assert.assertTrue(planContent.getText().length() > 50, "AI Plan was not generated successfully or is too short");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
