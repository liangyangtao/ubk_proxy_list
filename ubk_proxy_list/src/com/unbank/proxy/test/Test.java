package com.unbank.proxy.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.unbank.proxy.fetch.Fetcher;
import com.unbank.proxy.fetch.HttpClientBuilder;
import com.unbank.proxy.fetch.HttpClientFetcher;
import com.unbank.proxy.fetch.PhantomjsFetcher;
import com.unbank.proxy.quartz.task.SpiderConsole;

public class Test {

	private static Log logger = LogFactory.getLog(SpiderConsole.class);
	public static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
	public static BasicCookieStore cookieStore = new BasicCookieStore();

	public static void main(String[] args) {
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
		// ////////////////////////////////////////////////////////////////////
		HttpClientBuilder httpClientBuilder = new HttpClientBuilder(false,
				poolingHttpClientConnectionManager, cookieStore);
		CloseableHttpClient httpClient = httpClientBuilder.getHttpClient();
		HttpClientFetcher fetcher = new HttpClientFetcher(cookieStore,
				httpClient);
		// ////////////////////////////////////////////////////////////////////////
		// List<IpEntity> ipEntities = new SpysProxyDetailPaser()
		// .getProxyIpEntity(phantomjsFetcher);
		//
		// for (IpEntity ipEntity : ipEntities) {
		//
		// System.out.println(ipEntity.getIp());
		// System.out.println(ipEntity.getPort());
		// fetcher.setProxy(ipEntity.getIp(),
		// Integer.parseInt(ipEntity.getPort()));
		// String html = fetcher
		// .get("http://www.ip138.com/ip2city.asp", "gbk");
		// System.out.println(html);
		//
		// }

		// String url = "http://www.mszxyh.com/peweb/indexdb.do";
		// String html = fetcher.get(url);
		String url = "http://www.mszxyh.com/peweb/kjPage.do?id=ryb";
		String html = fetcher.get(url);
		Document document = Jsoup.parse(html, url);
		Elements iframeElemets = document.select("iframe");
		for (Element element : iframeElemets) {
			String href = element.absUrl("src");
			System.out.println(href);
			String tempHtml = phantomjsFetcher.get(href);
			element.appendChild(Jsoup.parse(tempHtml));
		}
		System.out.println(document);
		// httpclientGetHtml(fetcher);
		// phantomjsGetHtml(phantomjsFetcher);
		phantomjsFetcher.closeDriver();
		//
		// new SpiderConsole().inittask2();
		// driver.quit();
	}

	public static void phantomjsGetHtml(Fetcher phantomjsFetcher) {
		String html1 = phantomjsFetcher.get("http://www.ip138.com/ip2city.asp");
		System.out.println(html1);
		phantomjsFetcher.setProxy("183.207.228.9", "89");
		String html = phantomjsFetcher.get("http://www.ip138.com/ip2city.asp");
		System.out.println(html);
	}

	public static void httpclientGetHtml(Fetcher fetcher) {
		// 183.207.228.9:89
		// 111.161.126.99:80 透明代理
		// 58.251.78.71:8088
		// 222.89.224.142:8080
		fetcher.setProxy("222.89.224.142", "8080");
		String html = fetcher.get("http://www.ip138.com/ip2city.asp", "gbk");
		System.out.println(html);
	}

}
