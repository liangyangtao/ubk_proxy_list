package com.unbank.proxy.fetch;

public interface Fetcher {

	public String get(String url);

	public String get(String url, String charset);

	public void setProxy(String proxyIp, String proxyPort);

}
