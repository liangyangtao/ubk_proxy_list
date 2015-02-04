package com.unbank.proxy.fetch;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitFetcher implements Fetcher {

	private static Log logger = LogFactory.getLog(HtmlUnitFetcher.class);
	WebClient webClient;

	public HtmlUnitFetcher() {
		super();
	}

	public HtmlUnitFetcher(WebClient webClient) {
		this.webClient = webClient;
	}

	public static void main(String[] args) {
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setTimeout(10 * 1000);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.waitForBackgroundJavaScript(10 * 1000);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		String url = "http://spys.ru/free-proxy-list/CN/";
		String html = new HtmlUnitFetcher(webClient).get(url);
		System.out.println(html);
		webClient.closeAllWindows();
	}

	public String get(String url) {
		HtmlPage page = null;
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String html = page.asXml();
		return html;
	}

	public String get(String url, String charset) {
		return get(url);
	}

	public void setProxy(String proxyIp, String proxyPort) {
		ProxyConfig proxyConfig = new ProxyConfig(proxyIp,
				Integer.parseInt(proxyPort));
		webClient.getOptions().setProxyConfig(proxyConfig);
	}

}
