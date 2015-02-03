package com.unbank.proxy.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.unbank.proxy.quartz.task.SpiderConsole;


public class StartCrawlQuartzJobBeanQuartzJobBean {
	private static Log logger = LogFactory
			.getLog(StartCrawlQuartzJobBeanQuartzJobBean.class);

	public void executeInternal() {
		try {
			logger.info("重新启动定时任务");
			SpiderConsole spiderConsole = new SpiderConsole();
			spiderConsole.inittask();
		} catch (Exception e) {
			logger.info("检测内容节点出错", e);
		}
	}

}
