package com.unbank.proxy.paser.detail;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.unbank.proxy.entity.IpEntity;
import com.unbank.proxy.fetch.Fetcher;

public class SpysProxyDetailPaser {

	public List<IpEntity> getProxyIpEntity(Fetcher fetcher) {
		List<IpEntity> ipEntities = new ArrayList<IpEntity>();
		for (int i = 0; i <= 1; i++) {
			String url = "http://spys.ru/free-proxy-list/CN/" + i + "/";
			String html = fetcher.get(url);
			Document document = Jsoup.parse(html);
			Elements tableElements = document.select("tr.spy1xx");
			for (Element trElement : tableElements) {
				try {
					Element ipElement = trElement.select(
							"td:nth-child(1) > font:nth-child(2)").first();
					String ipElementText = ipElement.text();
					String temp[] = ipElementText.split(":");

					String ip = temp[0];
					String port = temp[1];
					IpEntity ipEntity = new IpEntity();
					ipEntity.setIp(ip);
					ipEntity.setPort(port);
					ipEntities.add(ipEntity);

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return ipEntities;

	}
}
