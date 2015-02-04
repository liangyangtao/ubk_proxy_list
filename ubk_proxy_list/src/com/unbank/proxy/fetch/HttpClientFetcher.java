package com.unbank.proxy.fetch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientFetcher implements Fetcher {
	private RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(30000).setConnectTimeout(30000).build();
	private BasicCookieStore cookieStore;
	private CloseableHttpClient httpClient;

	private final String _DEFLAUT_CHARSET = "utf-8";

	public HttpClientFetcher(BasicCookieStore cookieStore,
			CloseableHttpClient httpClient) {
		this.cookieStore = cookieStore;
		this.httpClient = httpClient;
	}

	private void fillHeader(String url, HttpGet httpGet) {
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 5.1; rv:29.0) Gecko/20100101 Firefox/29.0");
		httpGet.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpGet.setHeader("Accept-Language",
				"zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Referer", url);
		httpGet.setHeader("Cache-Control", "max-age=0");
	}

	public String get(String url) {
		return get(url, _DEFLAUT_CHARSET);
	}

	public String get(String url, String charset) {
		return get(url, null, charset);
	}

	public String get(String url, Map<String, String> headers, String charset) {
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		String useCharset = charset;
		if (charset == null) {
			useCharset = _DEFLAUT_CHARSET;
		}
		HttpGet httpGet = new HttpGet(url);
		fillHeader(url, httpGet);
		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet,
					context);
			try {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, useCharset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String post(String url, Map<String, String> params,
			Map<String, String> headers, String charset) {
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		String useCharset = charset;
		if (charset == null) {
			useCharset = _DEFLAUT_CHARSET;
		}
		try {
			HttpPost httpPost = new HttpPost(url);
			if (headers != null) {
				for (String key : headers.keySet()) {
					httpPost.setHeader(key, headers.get(key));
				}
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if (params != null) {
				for (String key : params.keySet()) {
					nvps.add(new BasicNameValuePair(key, params.get(key)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			}
			httpPost.setConfig(requestConfig);
			CloseableHttpResponse response = httpClient.execute(httpPost,
					context);
			try {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, useCharset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setProxy(String address, String port) {
		HttpHost proxy = new HttpHost(address, Integer.parseInt(port));
		requestConfig = RequestConfig.custom().setSocketTimeout(30000)
				.setConnectTimeout(30000).setProxy(proxy).build();
	}
}
