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
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import com.gargoylesoftware.htmlunit.javascript.host.dom.Document;

/**
 * 本教程演示如何利用WebCollector爬取javascript生成的数据
 *
 * @author hu
 */
public class DemoSelenium {

    static {
        //禁用Selenium的日志
        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.OFF);
    }


    public static void main(String[] args) throws Exception {
    	Proxys proxys=new Proxys();

		List<String[]> proxyList=cn.edu.hfut.dmic.webcollector.net.proxyController.readerProxyFromDir();
		System.out.println(proxyList.get(0).length);
		for(String[]tempProxy:proxyList){
			System.out.println(tempProxy[0]+" "+tempProxy[1]);
			proxys.add(tempProxy[0],Integer.valueOf(tempProxy[1]));
		}
		
        Executor executor=new Executor() {
            @Override
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
            	/*FirefoxProfile profile = new FirefoxProfile();
            	profile.setPreference("network.proxy.type", 1);
            	profile.setPreference("network.proxy.http", "localhost");
            	profile.setPreference("network.proxy.http_port", 3128);
            	WebDriver driver = new FirefoxDriver(profile);
            */

            	 System.setProperty("webdriver.chrome.driver", "D:/MyDrivers/chromedriver_win32/chromedriver.exe");

            	  WebDriver driver = new ChromeDriver();
            	  driver.get("http://www.ccgp.gov.cn/");
            	  Thread.sleep(5000);  // Let the user actually see something!
            	 Select select=new Select(driver.findElement(By.id("dbselect")));
            	 select.selectByValue("bidx");
            	  WebElement searchBox = driver.findElement(By.id("kw"));
            	 
            	  searchBox.sendKeys("视频会议");
            	  WebElement saveButton = driver.findElement(By.id("doSearch1"));
            	  saveButton.click();
            	  org.jsoup.nodes.Document doc = Jsoup.parse(driver.getPageSource());
            	  
            	   // Let the user actually see something!
            	  System.err.println( driver.getCurrentUrl());
            	  
            	  Thread.sleep(5000); 
            	  driver.quit();
            	/*  Thread.sleep(1000); 
              driver.close();
               */
                //System.err.println(proxy.toString()+"----"+strList[0]+":"+Integer.valueOf(strList[1]));
              
            
               
               
                		//findElementByCssSelector("span#outlink1");
               
            }
        };

        //创建一个基于伯克利DB的DBManager
        DBManager manager=new BerkeleyDBManager("crawl1111");
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler= new Crawler(manager,executor);
        crawler.addSeed("http://www.ccgp.gov.cn/");
        crawler.start(1);
    }

}
