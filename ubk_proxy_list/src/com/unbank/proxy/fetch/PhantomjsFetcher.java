package com.unbank.proxy.fetch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PhantomjsFetcher extends Fetcher {

	private static Log logger = LogFactory.getLog(PhantomjsFetcher.class);

	public WebDriver driver;

	public PhantomjsFetcher(WebDriver driver) {
		this.driver = driver;
	}

	

	public String get(String url) {
		String html = null;
		try {
			driver.get(url);
			waitForPageLoaded(driver);
			html = driver.getPageSource();

		} catch (Exception e) {

			if (e instanceof org.openqa.selenium.TimeoutException) {
				System.out.println(((JavascriptExecutor) driver)
						.executeScript("return document.readyState"));
				logger.info(url + "       " + "读取超时");
			} else {
				e.printStackTrace();
			}
		} finally {
			html = driver.getPageSource();
		}
		return html;
	}

	public void scroll() {
		((JavascriptExecutor) driver)
				.executeScript("window.scrollTo(0,document.body.scrollHeight)");
	}

	public void waitForPageLoaded(WebDriver driver) {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				System.out.println(((JavascriptExecutor) driver)
						.executeScript("return document.readyState"));
				return ((JavascriptExecutor) driver).executeScript(
						"return document.readyState").equals("complete");
			}
		};
		Wait<WebDriver> wait = new WebDriverWait(driver, 5000);
		try {
			wait.until(expectation);
		} catch (Throwable error) {
			logger.info(error);
		}
	}



	public String get(String url, String string) {
	
		return null;
	}



	public void setProxy(String string, int parseInt) {

		String PROXY = "115.231.96.120:80";
		org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
		proxy.setHttpProxy(PROXY).setFtpProxy(PROXY).setSslProxy(PROXY);
//		caps.setCapability(CapabilityType.PROXY, proxy);
	}

}
