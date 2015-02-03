package com.unbank.proxy.paser.detail;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.unbank.proxy.entity.IpEntity;
import com.unbank.proxy.fetch.HttpClientFetcher;

public class ProxzDetailPaser {

	public List<IpEntity> getProxyIpEntity(HttpClientFetcher fetcher) {
		String url = "http://www.proxz.com/proxy_list_high_anonymous_0_ext.html";
		String html = fetcher.get(url);
		Document document = Jsoup.parse(html);
		Element tableElement = document
				.select("body > div:nth-child(1) > center:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(3) > table:nth-child(4) > tbody:nth-child(1)")
				.first();
		List<IpEntity> ipEntities = new ArrayList<IpEntity>();

		Elements trElements = tableElement.select("tr");
		trElements.remove(0);
		for (Element trElement : trElements) {
			try {
				// System.out.println(trElement);
				Element ipElement = trElement.select("script").first();
				String ip = StringUtils.substringBetween(ipElement.toString(),
						"eval(unescape('", "'));</script>");
				ip = URLDecoder.decode(ip, "utf-8");
				ip = StringUtils.substringBetween(ip,
						"self.document.writeln(\"", "\");");
				Element portElement = trElement.select("td").get(1);
				String port = portElement.text().trim();
				IpEntity ipEntity = new IpEntity();
				ipEntity.setIp(ip);
				ipEntity.setPort(port);
				ipEntities.add(ipEntity);

			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
		return ipEntities;

	}

}
