package com.dev9.webtest.driver;

import com.dev9.webtest.util.SauceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * The purpose of this class is to provide a ThreadLocal instance of a WebDriver.
 *
 * @author <a href="mailto:Justin.Graham@dev9.com">Justin Graham</a>
 * @since 8/7/13
 */
public class ThreadLocalWebDriver implements WebDriver, JavascriptExecutor, HasInputDevices {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalWebDriver.class);
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
    private static ThreadLocal<Class> testClass = new ThreadLocal<Class>();
    private static ThreadLocal<TargetWebDriver> targetWebDriver = new ThreadLocal<TargetWebDriver>();
    private static ThreadLocal<String> jobId = new ThreadLocal<String>();

    public ThreadLocalWebDriver(Class clazz) {
        testClass.set(clazz);
        targetWebDriver.set(new TargetWebDriver(testClass.get()));
        init();
    }

    public ThreadLocalWebDriver(Class clazz, String testDescription) {
        testClass.set(clazz);
        TargetWebDriver targetDriver = new TargetWebDriver(clazz);

        if (testDescription != null && !testDescription.equals("")) {
            targetDriver.getCapabilities().setCapability("name", testDescription);
        }

        targetWebDriver.set(targetDriver);
        init();
    }

    private void init() {
        setDriver(targetWebDriver.get());
        setJobId();
        reportURL();
    }

    private void setDriver(TargetWebDriver d) {
        driver.set(d.build());
    }

    private void setJobId() {
        jobId.set(SauceUtils.getJobId(driver.get()));
    }

    public String getJobUrl() {
        return SauceUtils.getJobUrl(driver.get());
    }

    public String getJobId() {
        return jobId.get();
    }

    public boolean instanceOf(Class clazz) {
        return clazz.isAssignableFrom(driver.get().getClass());
    }

    public boolean isRemote() {
        return targetWebDriver.get().isRemote();
    }

    public Browser getBrowser() {
        return targetWebDriver.get().getBrowser();
    }

    public String getSessionId() {
        if (driver.get() instanceof RemoteWebDriver) {
            return ((RemoteWebDriver) driver.get()).getSessionId().toString();
        }
        return null;
    }

    @Override
    public void get(String s) {
        driver.get().get(s);
    }

    /* ==============================================================================
                                      WebDriver Interface
       ============================================================================== */

    @Override
    public String getCurrentUrl() {
        return driver.get().getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.get().getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.get().findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return driver.get().findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.get().getPageSource();
    }

    @Override
    public void close() {
        driver.get().close();
    }

    @Override
    public void quit() {
        driver.get().quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.get().getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.get().getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return driver.get().switchTo();
    }

    @Override
    public Navigation navigate() {
        return driver.get().navigate();
    }

    @Override
    public Options manage() {
        return driver.get().manage();
    }

    @Override
    public Object executeScript(String s, Object... objects) {
        return ((JavascriptExecutor) driver.get()).executeScript(s, objects);
    }

    /* ==============================================================================
                                JavascriptExecutor Interface
       ============================================================================== */

    @Override
    public Object executeAsyncScript(String s, Object... objects) {
        return ((JavascriptExecutor) driver.get()).executeAsyncScript(s, objects);
    }

    @Override
    public Keyboard getKeyboard() {
        return ((HasInputDevices) driver.get()).getKeyboard();
    }

    /* ==============================================================================
                                  HasInputDevices Interface
       ============================================================================== */

    @Override
    public Mouse getMouse() {
        return ((HasInputDevices) driver.get()).getMouse();
    }

    private void reportURL() {
        if (targetWebDriver.get().isRemote()) {
            LOG.info("Remote job url: {}", getJobUrl());
        }
    }
}
