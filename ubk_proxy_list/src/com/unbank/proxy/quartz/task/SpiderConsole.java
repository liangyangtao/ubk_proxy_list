package com.unbank.proxy.quartz.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.unbank.proxy.entity.IpEntity;
import com.unbank.proxy.fetch.Fetcher;
import com.unbank.proxy.fetch.HttpClientBuilder;
import com.unbank.proxy.fetch.HttpClientFetcher;
import com.unbank.proxy.fetch.PhantomjsFetcher;
import com.unbank.proxy.paser.detail.SpysProxyDetailPaser;

public class SpiderConsole {

	private static Log logger = LogFactory.getLog(SpiderConsole.class);
	public static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
	public static BasicCookieStore cookieStore = new BasicCookieStore();

	public void inittask() {
		// DesiredCapabilities caps = new DesiredCapabilities();
		// caps.setJavascriptEnabled(true);
		// caps.setCapability(
		// PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
		// "phantomjs-1.9.7-windows/phantomjs.exe");
		// WebDriver driver = new PhantomJSDriver(caps);
		// long timeout = 5000;
		// TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		// driver.manage().timeouts().pageLoadTimeout(timeout, timeUnit);
		PhantomjsFetcher phantomjsFetcher = new PhantomjsFetcher();

		List<IpEntity> ipEntities = new SpysProxyDetailPaser()
				.getProxyIpEntity(phantomjsFetcher);
		HttpClientBuilder httpClientBuilder = new HttpClientBuilder(false,
				poolingHttpClientConnectionManager, cookieStore);
		CloseableHttpClient httpClient = httpClientBuilder.getHttpClient();
		HttpClientFetcher fetcher = new HttpClientFetcher(cookieStore,
				httpClient);
		String myip = fetcher.get("http://www.ip138.com/ip2city.asp", "gbk");
		List<IpEntity> availableIps = new ArrayList<IpEntity>();
		for (IpEntity ipEntity : ipEntities) {
			fetcher.setProxy(ipEntity.getIp(), ipEntity.getPort());
			String html = fetcher
					.get("http://www.ip138.com/ip2city.asp", "gbk");
			if (myip.equals(html)) {
				continue;
			}
			availableIps.add(ipEntity);
		}
		for (IpEntity ipEntity : availableIps) {
			System.out.println(ipEntity.getIp());
			System.out.println(ipEntity.getPort());
		}
		phantomjsFetcher.closeDriver();
	}

}
