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
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BerkeleyDBManager;
import cn.edu.hfut.dmic.webcollector.util.FileIO;

import java.io.InputStream;
import java.net.Proxy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * 本教程演示如何利用WebCollector爬取javascript生成的数据
 *
 * @author hu
 */
public class DemoSelenium {

	static {
		// 禁用Selenium的日志
		Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
		//logger.setLevel(Level.OFF);
		logger = Logger.getLogger("cn.edu.hfut.dmic.webcollector.fetcher.Fetcher");
		logger.setLevel(Level.OFF);
	}

	public static void main(String[] args) {
		ArrayList<String> urlList = new ArrayList<>();
		Proxys proxys = new Proxys();

		List<String[]> proxyList = cn.edu.hfut.dmic.webcollector.net.proxyController.readerProxyFromDir();
		System.out.println(proxyList.get(0).length);
		for (String[] tempProxy : proxyList) {
			System.out.println(tempProxy[0] + " " + tempProxy[1]);
			proxys.add(tempProxy[0], Integer.valueOf(tempProxy[1]));
		}

		Executor executor = new Executor() {
			@Override
			public void execute(CrawlDatum datum, CrawlDatums next) {
				 System.setProperty("webdriver.gecko.driver",
				 "D:\\MyDrivers\\geckodriver-v0.11.1-win64\\geckodriver.exe");
				/*System.setProperty("webdriver.gecko.driver",
						"D:\\Softwares\\geckodriver-v0.11.1-win64\\geckodriver.exe");
				System.setProperty("webdriver.firefox.bin", "D:\\Softwares\\firefox\\firefox.exe");*/
				boolean openedFlag = false;
				while (!openedFlag) {
					java.net.Proxy proxy = proxys.nextRandom();

					String[] proxyarray = proxy.toString().split(":");
					String proxyHost = proxyarray[0].split("/")[1];
					int proxyPort = Integer.valueOf(proxyarray[1]);

					// 检测该代理是否可用telnet检测方式
					// TelnetClient telnetClient = new TelnetClient("vt200");
					// //指明Telnet终端类型，否则会返回来的数据中文会乱码
					// telnetClient.setDefaultTimeout(3000); //socket延迟时间：1000ms
					// System.err.println("try proxy " + proxy);
					// try {
					// telnetClient.connect(proxyHost,proxyPort);
					// } catch (IOException e1) {
					// proxys.remove(proxy);
					// System.err.println("remove proxy : " + proxy);
					// //e1.printStackTrace();
					// continue;
					// }

					// 代理检测方式 121.204.165.29:8118
					// proxyHost = "121.204.165.29";
					// proxyPort = 8118;

					try {
						Proxy tmpproxy = new Proxy(java.net.Proxy.Type.HTTP,
								new InetSocketAddress(proxyHost, proxyPort));
						URL u = new URL("http://www.ccgp.gov.cn/");
						HttpURLConnection con = (HttpURLConnection) u.openConnection(tmpproxy);
						con.setConnectTimeout(3000);
						con.setReadTimeout(3000);
						InputStream inStrm = con.getInputStream();
						StringBuffer out = new StringBuffer();
						byte[] b = new byte[4096];
						for (int n; (n = inStrm.read(b)) != -1;) {
							out.append(new String(b, 0, n));
						}
						// System.out.println("page is " + out.toString());
						if (!out.toString().contains("<title>中国政府采购网_首页</title>")) {
							proxys.remove(proxy);
							System.err.println("remove proxy : " + proxy);
							continue;
						}
					} catch (Exception e) {
						proxys.remove(proxy);
						System.err.println("remove proxy : " + proxy);
						continue;
					}

					System.err.println("use proxy : " + proxy);

					/*
					 * String proxyHost = "222.211.53.201"; int proxyPort =
					 * 8118;
					 */
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
					// profile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so",
					// false);
					// 禁用css,不方便调试了。。
					// fireFoxProfile.setPreference("permissions.default.stylesheet",
					// 2);
					// 启动快速加载，不过好像没什么改变。照官方说法在load结束前就可以开始操作，不过我这还是被blocked直到页面加载完毕
					// profile.setPreference("webdriver.load.strategy", "fast");

					WebDriver driver = new FirefoxDriver(profile);

					try {
						/**
						 * driver.get("http://www.ccgp.gov.cn/"); Select select
						 * = new Select(driver.findElement(By.id("dbselect")));
						 * select.selectByValue("bidx");
						 * 
						 * WebElement searchPage =
						 * driver.findElement(By.name("page_index"));
						 * JavascriptExecutor jse = (JavascriptExecutor) driver;
						 * // 这种方式可用直接给隐藏域赋值 String pageChange =
						 * "document.getElementById('page_index').value='2'";
						 * String startTimeChange =
						 * "document.getElementById('start_time').value='2016:1:1'";
						 * String endTimeChange =
						 * "document.getElementById('end_time').value='2016:6:30'";
						 * jse.executeScript(pageChange);
						 * jse.executeScript(startTimeChange);
						 * jse.executeScript(endTimeChange); WebElement
						 * searchBox = driver.findElement(By.id("kw"));
						 * searchBox.sendKeys("视频");
						 * 
						 * System.err.println("searchPage.value = " +
						 * searchPage.getAttribute("value")); WebElement
						 * saveButton = driver.findElement(By.id("doSearch1"));
						 * saveButton.click(); System.out.println(
						 * "Page title 1 is: " + driver.getTitle());
						 * 
						 * (new WebDriverWait(driver, 10)).until(new
						 * ExpectedCondition<Boolean>() { public Boolean
						 * apply(WebDriver d) { WebElement searchBox =
						 * driver.findElement(By.id("inpProjectId")); if
						 * (searchBox == null) { return false; } else { return
						 * true; } } });
						 * 
						 * org.jsoup.nodes.Document doc =
						 * Jsoup.parse(driver.getPageSource());
						 * System.err.println("driver.URL　＝　" +
						 * driver.getCurrentUrl()); System.out.println(
						 * "Page title 2 is: " + driver.getTitle()); //
						 * System.err.println("doc.text() ＝ " + doc.html());
						 */
						
						int i = 1;
						String lasturl="http://search.ccgp.gov.cn/dataB.jsp?searchtype=1&page_index=1&buyerName=&projectId=&dbselect=infox&kw=&start_time=2016%3A01%3A01&end_time=2016%3A06%3A29&timeType=6&bidSort=2&pinMu=0&bidType=1&displayZone=&zoneId=&pppStatus=&agentName=";
						driver.get(lasturl);
						Thread.sleep(10 * 1000);
						while (i <= 8072) {
							System.err.println(i + "th titile is :" + driver.getCurrentUrl());
							final int flag = i;
							System.err.println("wait page load");
//							(new WebDriverWait(driver, 15)).until(new ExpectedCondition<Boolean>() {
//								public Boolean apply(WebDriver d) {
//									WebElement nextButton = driver.findElement(By.className("next"));
//									String nexthref = nextButton.getAttribute("href");
//									System.err.println("nextIndex = " + nexthref);
//									String[] tmp = StringUtils.split(nexthref, "&");
//									String nextIndex = "";
//									if (tmp.length > 3) {
//										nextIndex = tmp[1];
//									}
//									nextIndex = StringUtils.substringAfter(nextIndex, "page_index=");
//									int nextInt = Integer.valueOf(nextIndex);
//									int currentInt = nextInt - 1;
//									System.out.println("currentInt = " + currentInt);
//								
//									if (currentInt == flag) {
//										return true;
//									} else {
//										return false;
//									}
//								}
//							});
							//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
							
							
							System.err.println("page load done");
							org.jsoup.nodes.Document doc=Jsoup.parse(driver.getPageSource());
							 Elements elements=doc.getElementsByAttribute("href");
								
								String linkContent="";
								for(Element element:elements){
									if(element.attr("href").matches("http://www.ccgp.gov.cn/cggg/.*htm"))
									{System.out.println(element.attr("href"));
									
									linkContent+=element.attr("href")+'\n';}
								}
							FileIO.saveintoFile("E:/data/china/links/"+i,linkContent);
							
							WebElement nextButton = driver.findElement(By.className("next"));
							System.err.println("click begin");
							nextButton.click();
							Thread.sleep(10000);
							while(driver.getCurrentUrl().equals(lasturl))
								Thread.sleep(1000);
							lasturl=driver.getCurrentUrl();
							System.err.println("click done");
							i++;
						}

						Thread.sleep(1000);
						openedFlag = true;
					} catch (Exception e) {
						driver.quit();
						driver.close();
						openedFlag = false;
						proxys.remove(proxy);
						e.printStackTrace();
					}
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
