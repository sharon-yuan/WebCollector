/*
 * Copyright (C) 2015 hu
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
package cn.edu.hfut.dmic.webcollector.example;

import cn.edu.hfut.dmic.webcollector.crawldb.DBManager;
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BerkeleyDBManager;

import java.io.File;
import java.lang.reflect.Proxy;
import java.sql.Driver;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.javascript.host.dom.Document;

/**
 * 本教程演示如何利用WebCollector爬取javascript生成的数据
 *
 * @author hu
 */
public class DemoSelenium {

	static {
		// 禁用Selenium的日志
		Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
		logger.setLevel(Level.OFF);
		logger = Logger.getLogger("cn.edu.hfut.dmic.webcollector.fetcher.Fetcher");
		logger.setLevel(Level.OFF);
	}

	public static void main(String[] args) {
		// Proxys proxys = new Proxys();
		//
		// List<String[]> proxyList =
		// cn.edu.hfut.dmic.webcollector.net.proxyController.readerProxyFromDir();
		// System.out.println(proxyList.get(0).length);
		// for (String[] tempProxy : proxyList) {
		// System.out.println(tempProxy[0] + " " + tempProxy[1]);
		// proxys.add(tempProxy[0], Integer.valueOf(tempProxy[1]));
		// }

		Executor executor = new Executor() {
			@Override
			public void execute(CrawlDatum datum, CrawlDatums next) {
				System.setProperty("webdriver.gecko.driver",
						"D:\\Softwares\\geckodriver-v0.11.1-win64\\geckodriver.exe");
				// java.net.Proxy proxy = proxys.nextRandom();
				// System.err.println(proxy);
				// String[] proxyarray = proxy.toString().split(":");
				System.setProperty("webdriver.firefox.bin", "D:\\Softwares\\firefox\\firefox.exe");
				String proxyHost = "222.211.53.201";
				int proxyPort = 8118;
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
				profile.setPreference("webdriver.load.strategy", "unstable");

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
				} catch (InterruptedException e) {
					driver.quit();
					driver.close();
				}

			}
		};

		// 创建一个基于伯克利DB的DBManager
		DBManager manager = new BerkeleyDBManager("crawl1111");
		// 创建一个Crawler需要有DBManager和Executor
		Crawler crawler = new Crawler(manager, executor);
		crawler.addSeed("http://www.ccgp.gov.cn/");
		try {
			crawler.start(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
