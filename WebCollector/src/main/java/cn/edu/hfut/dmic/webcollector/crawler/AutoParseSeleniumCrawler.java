/*
 * Copyright (C) 2014 hu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package cn.edu.hfut.dmic.webcollector.crawler;

import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.fetcher.Visitor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;

import java.lang.reflect.Proxy;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hu
 */
public abstract class AutoParseSeleniumCrawler extends Crawler implements Executor, Visitor, Requester {

	public static final Logger LOG = LoggerFactory.getLogger(AutoParseSeleniumCrawler.class);

	/**
	 * 是否自动抽取符合正则的链接并加入后续任务
	 */
	protected boolean autoParse = true;

	protected Visitor visitor;
	protected Requester requester;
	public static Proxys proxys = new Proxys();

	public AutoParseSeleniumCrawler(boolean autoParse) {
		this.autoParse = autoParse;
		this.visitor = this;
		this.requester = this;
		this.executor = this;

		List<String[]> proxyList = cn.edu.hfut.dmic.webcollector.net.proxyController.readerProxyFromDir();
		System.out.println(proxyList.get(0).length);
		for (String[] tempProxy : proxyList) {
			System.out.println(tempProxy[0] + " " + tempProxy[1]);
			proxys.add(tempProxy[0], Integer.valueOf(tempProxy[1]));
		}

	}

	@Override
	public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
		HttpRequest request = new HttpRequest(crawlDatum, proxys.nextRandom());

		return request.getResponse();
	}

	public static void badProxy(java.net.Proxy proxy) {
		System.err.println(proxy);
		proxys.remove(proxy.toString());

	}

	/**
	 * URL正则约束
	 */
	protected RegexRule regexRule = new RegexRule();

	@Override
	public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
		System.setProperty("webdriver.gecko.driver", "D:\\MyDrivers\\geckodriver-v0.11.1-win64\\geckodriver.exe");
		java.net.Proxy proxy = proxys.nextRandom();
		 System.err.println(proxy);
		String[] proxyarray = proxy.toString().split(":");
		 String proxyHost = proxyarray[0];
			int proxyPort = Integer.valueOf(proxyarray[1]);
		/*String proxyHost = "222.211.53.201";
		int proxyPort = 8118;*/
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.http", proxyHost);
		profile.setPreference("network.proxy.http_port", proxyPort);
		profile.setPreference("network.proxy.ssl", proxyHost);
		profile.setPreference("network.proxy.ssl_port", Integer.valueOf(proxyPort));
		profile.setPreference("network.proxy.share_proxy_settings", false);
		profile.setPreference("network.proxy.no_proxies_on", "localhost");
		profile.setPreference("permissions.default.image", 2);

		// 关掉flash
		profile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);
		// 禁用css,不方便调试了。。
		// fireFoxProfile.setPreference("permissions.default.stylesheet",
		// 2);
		// 启动快速加载，不过好像没什么改变。照官方说法在load结束前就可以开始操作，不过我这还是被blocked直到页面加载完毕
		profile.setPreference("webdriver.load.strategy", "fast");

		WebDriver driver = new FirefoxDriver(profile);
		// WebDriver driver = new FirefoxDriver();
		try {
			driver.get("http://www.ccgp.gov.cn/");
			Select select = new Select(driver.findElement(By.id("dbselect")));
			select.selectByValue("bidx");

			WebElement searchPage = driver.findElement(By.name("page_index"));
			JavascriptExecutor jse = (JavascriptExecutor) driver;
			// 这种方式可用直接给隐藏域赋值
			String pageChange = "document.getElementById('page_index').value='2'";
			String startTimeChange = "document.getElementById('start_time').value='2016:1:1'";
			String endTimeChange = "document.getElementById('end_time').value='2016:6:30'";
			jse.executeScript(pageChange);
			jse.executeScript(startTimeChange);
			jse.executeScript(endTimeChange);

			WebElement searchBox = driver.findElement(By.id("kw"));
			searchBox.sendKeys("视频");

			System.err.println("searchPage.value = " + searchPage.getAttribute("value"));
			WebElement saveButton = driver.findElement(By.id("doSearch1"));
			System.err.println("click.");
			saveButton.click();
			System.err.println("click done");
			System.out.println("Page title 1 is: " + driver.getTitle());

			(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					WebElement searchBox = driver.findElement(By.id("inpProjectId"));
					if (searchBox == null) {
						return false;
					} else {
						return true;
					}
				}
			});

			org.jsoup.nodes.Document doc = Jsoup.parse(driver.getPageSource());
			System.err.println("driver.URL　＝　" + driver.getCurrentUrl());
			System.out.println("Page title 2 is: " + driver.getTitle());
			System.err.println("doc.text()　＝　" + doc.html());
			Thread.sleep(1000);
			Links links = new Links().addByRegex(doc, regexRule);
			next.add(links);
		} catch (InterruptedException e) {
			driver.quit();
			driver.close();
		}

		
//

    	/*if(requester==null){System.err.println("AutoParseCrawler.execute requester == null");next.add(datum);throw new Exception();}
    	System.err.println("1");
        HttpResponse response = requester.getResponse(datum);
        System.err.println("3");
        Page page = new Page(datum, response);
      System.err.println(response);
      if(response==null){System.err.println("AutoParseCrawler.execute response == null");next.add(datum);throw new Exception();}
        visitor.visit(page, next);
        if (autoParse && !regexRule.isEmpty()) {
            parseLink(page, next);
        }
        afterParse(page, next);*/
    
	}

	protected void afterParse(Page page, CrawlDatums next) {
		if (page != null)
			System.out.println(page.doc().text());
		else
			System.err.println("-------===>>>AutoParseCrawler.afterParse page==nul<<<=====---------");
	}

	protected void parseLink(Page page, CrawlDatums next) {
		String conteType = page.getResponse().getContentType();
		if (conteType != null && conteType.contains("text/html")) {
			Document doc = page.getDoc();
			if (doc != null) {
				Links links = new Links().addByRegex(doc, regexRule);
				next.add(links);
			}
		}

	}

	/**
	 * 添加URL正则约束
	 *
	 * @param urlRegex
	 *            URL正则约束
	 */
	public void addRegex(String urlRegex) {
		regexRule.addRule(urlRegex);
	}

	/**
	 *
	 * @return 返回是否自动抽取符合正则的链接并加入后续任务
	 */
	public boolean isAutoParse() {
		return autoParse;
	}

	/**
	 * 设置是否自动抽取符合正则的链接并加入后续任务
	 *
	 * @param autoParse
	 *            是否自动抽取符合正则的链接并加入后续任务
	 */
	public void setAutoParse(boolean autoParse) {
		this.autoParse = autoParse;
	}

	/**
	 * 获取正则规则
	 *
	 * @return 正则规则
	 */
	public RegexRule getRegexRule() {
		return regexRule;
	}

	/**
	 * 设置正则规则
	 *
	 * @param regexRule
	 *            正则规则
	 */
	public void setRegexRule(RegexRule regexRule) {
		this.regexRule = regexRule;
	}

	/**
	 * 获取Visitor
	 *
	 * @return Visitor
	 */
	public Visitor getVisitor() {
		return visitor;
	}

	/**
	 * 设置Visitor
	 *
	 * @param visitor
	 *            Visitor
	 */
	public void setVisitor(Visitor visitor) {
		this.visitor = visitor;
	}

	public Requester getRequester() {
		return requester;
	}

	public void setRequester(Requester requester) {
		this.requester = requester;
	}
}
