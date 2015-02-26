package com.unbank.proxy.test;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.unbank.proxy.fetch.HttpClientBuilder;
import com.unbank.proxy.fetch.HttpClientFetcher;

public class ContentFilter {

	public Logger logger = Logger.getLogger(ContentFilter.class);
	public static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
	public static BasicCookieStore cookieStore = new BasicCookieStore();

	public static void main(String[] args) {
		HttpClientBuilder httpClientBuilder = new HttpClientBuilder(false,
				poolingHttpClientConnectionManager, cookieStore);
		CloseableHttpClient httpClient = httpClientBuilder.getHttpClient();
		HttpClientFetcher fetcher = new HttpClientFetcher(cookieStore,
				httpClient);
		// String url = "http://bank.hexun.com/2015-02-26/173549425.html";
		String url = "http://www.safe.gov.cn/wps/portal/!ut/p/c5/04_SB8K8xLLM9MSSzPy8xBz9CP0os3gPZxdnX293QwML7zALA09P02Bnr1BvIyNvc6B8pFm8s7ujh4m5j4GBhYm7gYGniZO_n4dzoKGBpzEB3eEg-_DrB8kb4ACOBvp-Hvm5qfoFuREGWSaOigDuOwR_/dl3/d3/L2dJQSEvUUt3QS9ZQnZ3LzZfSENEQ01LRzEwODRJQzBJSUpRRUpKSDEySTI!/?WCM_GLOBAL_CONTEXT=/wps/wcm/connect/safe_web_store/safe_web/zcfg/whkjgl/node_zcfg_whkj/8680000047532a8c8147a73b4795588d";
		String html = fetcher.get(url);
		Document document = Jsoup.parse(html, url);
		String content = new ContentFilter().extractContent(document);
		System.out.println(content);

	}

	public String extractContent(Document document) {
		// 去除广告节点
		if (document == null) {
			return null;
		}
		Element contentbody = getContentElement(document);
		// 格式化内容节点
		String content = contentbody.toString();
		content = formatContent(content);
		return content;

	}

	private String formatContent(String content) {
		content = replaceStockCode(content);
		content = replaceSpechars(content);
		return content;
	}

	private Element getContentElement(Document document) {
		// 去掉不需要的HTML标签
		removeNoNeedElementsByCssQuery(document.body());
		// 获取内容字数最多的节点
		Element contentbody = getContentBody(document);

		formatImage(contentbody);

		// 去除内容中的广告链接节
		removeAdvertiseLink(contentbody);
		// 去重内容中的推广文字
		removeAdvertiseText(contentbody);
		// 格式化内容节点
		formatElements(contentbody);
		return contentbody;
	}

	private void formatImage(Element contentbody) {
		Elements elements = contentbody.select("img");
		for (Element element : elements) {
			if ("display:none;".equals(element.attr("style"))) {
				element.remove();
				continue;
			}
			String imgSrc = element.absUrl("src");
			element.attr("src", imgSrc);
			Attributes attributes = element.attributes();
			for (Attribute attribute : attributes) {
				if (attribute.getKey().isEmpty()) {
					continue;
				} else if (attribute.getKey().equals("src")) {
					continue;
				} else {
					element.removeAttr(attribute.getKey());
				}
			}
		}

	}

	// 去掉不需要的HTML标签
	public void removeNoNeedElementsByCssQuery(Element contentElement) {
		String cssQuerys[] = new String[] { "script", "style", "textarea",
				"select", "noscript", "input" };
		for (String string : cssQuerys) {
			removeNoNeedElement(contentElement, string);
		}
	}

	private Element getContentBody(Element body) {

		Element contentElement = getMaxLengthChild(body);
		System.out.println(contentElement);
		Element bodyElement = body.clone();
		removeAdvertiseLink(bodyElement);
		Element parentElement = contentElement.parent();
		while (true) {
			float size = (float) parentElement.text().length()
					/ bodyElement.text().length();
			System.out.println(size);
			if (size < 0.26) {
				parentElement = parentElement.parent();
			} else {
				break;
			}
		}
		return parentElement;
	}

	// 获取内容文字 最多的节点
	public Element getMaxLengthChild(Element parentElement) {
		Element temp = parentElement;
		if (temp == null) {
			return null;
		}

		while (true) {
			Elements childElements = temp.children();
			for (int i = 0; i < childElements.size() - 1; i++) {
				for (int j = i + 1; j < childElements.size(); j++) {
					// 如果包含标题
					int iLength = getElemnetTextLength(childElements.get(i)
							.text());
					int jLength = getElemnetTextLength(childElements.get(j)
							.text());
					if (iLength < jLength) {
						Element tempElemnt = childElements.get(i);
						childElements.set(i, childElements.get(j));
						childElements.set(j, tempElemnt);
					}
				}
			}
			// 找到结果最大的那个
			temp = childElements.first();
			if (temp != null && temp.text().trim().length() <= 50) {
				temp = getIncludeTextNode(temp);
				break;
			}
			if (temp.children().size() == 0) {
				if (temp.text().trim().length() <= 50) {
					temp = getIncludeTextNode(temp);
				}
				break;
			}
		}
		return temp;
	}

	// 获取包含文字的父节点
	public Element getIncludeTextNode(Element temp) {
		while (true) {
			temp = temp.parent();
			if (temp == null) {
				break;
			}
			if (temp.text().trim().length() >= 50) {
				break;
			}

		}
		return temp;
	}

	// 获取文本的长度
	private int getElemnetTextLength(String itext) {
		int iLength = 0;
		char[] iarray = itext.toCharArray();
		for (char c : iarray) {
			if (c == ',' || c == '，') {
				iLength = iLength + 10;
			} else if (c == '。') {
				iLength = iLength + 30;
			} else {
				iLength = iLength + 1;
			}
		}
		return iLength;
	}

	public void removeAdvertiseNode(Element contentElement) {
		removeAdvertiseNodes(contentElement);
	}

	public void removeAdvertiseNodes(Element contentElement) {
		removeNoNeedElementsByCssQuery(contentElement);

	}

	public void removeNoNeedElementsByText(Element contentElement) {
		String textQuerys[] = new String[] { "" };
		for (String string : textQuerys) {
			removeNoNeedTextElement(contentElement, string);
		}
	}

	public void formatElements(Element contentElement) {
		// 去重属性
		removeElementAttr(contentElement);
		Elements allElements = contentElement.children();
		for (Element element : allElements) {
			removeElementAttr(element);
			changeNodeName(element);
			if (element != null) {
				formatElements(element);
			}

		}

	}

	public void changeNodeName(Element element) {
		if (element == null) {
			return;
		}
		if (element.tagName().equals("p") || element.tagName().equals("div")) {
			element.tagName("p");
		} else if (element.tagName().equals("b")) {
			element.tagName("strong");
		} else {
			if (element.tagName().equals("br")
					|| element.tagName().equals("img")
					|| element.tagName().equals("table")
					|| element.tagName().equals("th")
					|| element.tagName().equals("tr")
					|| element.tagName().equals("td")
					|| element.tagName().equals("tbody")
					|| element.tagName().equals("strong")
					|| element.tagName().equals("center")) {
			} else {
				element.tagName("v");
			}
		}
	}

	// 移除所有的属性
	public void removeElementAttr(Element element) {
		if (element == null) {
			return;
		}
		Attributes attributes = element.attributes();
		for (Attribute attribute : attributes) {
			if (attribute.getKey().isEmpty()) {
				continue;
			} else if (attribute.getKey().equals("align")
					&& attribute.getValue().equals("center")) {
				continue;
			} else if (attribute.getKey().equals("style")
					&& (attribute.getValue().toLowerCase()
							.contains("text-align: center"))) {
				continue;
			} else if (attribute.getKey().equals("rowspan")
					|| attribute.getKey().equals("colspan")
					|| attribute.getKey().equals("src")) {
				continue;
			} else {
				element.removeAttr(attribute.getKey());
			}
		}
	}

	public void removeAdvertiseText(Element maxTextElement) {
		String advertiseTexts[] = new String[] { "相关新闻", "相关评论", "相关专题",
				"重点推荐", "延伸阅读", "推荐阅读", "相关报道", "商业专栏", "证券要闻", "行业动态", "公司新闻",
				"相关新闻", "往期回顾", "今日消息", "机构策略", "相关阅读", "免责声明", "版权声明", "相关链接",
				"机构研究", "个股点评", "行业新闻", "公司动态" };
		for (String string : advertiseTexts) {
			removeNoNeedTextElement(maxTextElement, string);
		}

	}

	public void removeAdvertiseLink(Element maxTextElement) {
		if (maxTextElement == null) {
			return;
		}
		Elements elements = maxTextElement.select("a");
		for (Element element : elements) {
			if (element.text().length() > 10) {
				element.remove();
			}
		}
	}

	public String replaceStockCode(String content) {

		try {
			content = content.replaceAll(">\\s{0,10}", ">");
			content = content.replaceAll(">\\s{0,10}(&nbsp; ){0,}", ">");
			content = content.replaceAll(">\\s{0,10}(&nbsp;){0,}", ">");
			content = content.replaceAll(">\\s{0,10} {0,}", ">");
			content = content.replaceAll(">\\s{0,10}  {0,}", ">");
			content = content.replaceAll(">\\s{0,10}", ">");
			content = content.replaceAll("\\s{0,10}<", "<");
			content = content.replaceAll("<br>", "</p><p>");
			content = content.replaceAll("<br />", "</p><p>");
			content = content.replaceAll("<br/>", "</p><p>");
			content = content.replace("<v>", "");
			content = content.replace("</v>", "");
			content = content.replaceAll("<p></p>", "");
			content = removeStockCode(content);
			content = content.trim();
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
		return content;
	}

	public String removeStockCode(String content) {
		String stockCodes[] = new String[] { "<!--.[^-]*(?=-->)-->",
				"(?is)<!--.*?-->",

				"\\(([^\\(]*)?微博([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?基金吧([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?股吧([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?代码([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?记者([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?编辑([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?作者([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?点击([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?访问([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?来源([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?标题([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?微信([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?收盘价([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?客户端([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?交易所([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?行情([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?评论([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?声明([^\\(|\\)]*)?\\)",
				"\\(([^\\(]*)?版权([^\\(|\\)]*)?\\)",

		};
		for (String string : stockCodes) {
			content = content.replaceAll(string, "");
		}
		return content;
	}

	public String replaceSpechars(String content) {
		content = replaceNoNeedChars(content);
		return content;
	}

	// 去掉文章中不需要的文字
	public String replaceNoNeedChars(String content) {
		String spechars[] = new String[] { "我有话说", "欢迎发表评论", "发表评论", "收藏本文",
				"微博推荐", "[微博]", "(财苑)", "返回列表", "加入收藏", "打印本页", "打印本稿", "新闻订阅",
				"分享到", "【打印】", "网站论坛", "字体：大 中 小", "字体:大 中 小", "字体：", "字体:",
				"推荐朋友", "关闭窗口", "关闭", "慧聪资讯手机客户端下载", "(市值重估)",
				"[最新消息 价格 户型 点评]", "[简介 最新动态]", "[简介最新动态]", "[最新消息价格户型点评]",
				"(楼盘)", "(点击查看最新人物消息)", "( 详情 图库 团购 点评 ) ",
				"(CNFIN.COM/XINHUA08.COM)--", "(CNFIN.COM / XINHUA08.COM)--",
				"(CNFIN.COM&nbsp;/&nbsp;XINHUA08.COM)--", "(看跌期权)", "(放心保)",
				"(查询信托产品)", "(楼盘资料)", "点击浏览全文", "返回首页", "《》：", "(滚动资讯)", "()",
				"[]", "【】", "【点击查看全文】" };

		for (String string : spechars) {
			content = content.replace(string, "");
		}
		return content;
	}

	// 去掉不想要的html 标签
	public void removeNoNeedElement(Element element, String cssQuery) {
		if (element == null) {
			return;
		}
		Elements elements = element.select(cssQuery);
		for (Element element2 : elements) {
			element2.remove();
		}
	}

	// 去重不想要的文字节点
	public void removeNoNeedTextElement(Element element, String textQuery) {
		if (element == null) {
			return;
		}
		Elements element2 = element.getElementsContainingOwnText(textQuery);
		for (Element element3 : element2) {
			element3.remove();
		}
	}

}
