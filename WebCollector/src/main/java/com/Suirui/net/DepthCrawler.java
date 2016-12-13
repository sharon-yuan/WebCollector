package com.Suirui.net;
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


import cn.edu.hfut.dmic.webcollector.crawler.AutoParseSeleniumCrawler;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BerkeleyDBManager;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

/**
 * 本教程和深度遍历没有任何关系
 * 一些爬取需求希望加入深度信息，即遍历树中网页的层
 * 利用2.20版本中的新特性MetaData可以轻松实现这个功能
 * 
 * @author hu
 */
public class DepthCrawler extends AutoParseSeleniumCrawler{

    public DepthCrawler(String crawlPath, boolean autoParse) {
        super( autoParse);
        this.dbManager=new BerkeleyDBManager(crawlPath);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
    	
        System.out.println("visiting:"+page.getUrl()+"\tdepth="+page.meta("depth"));
        System.err.println(page.getHtml());
    }

    @Override
    protected void afterParse(Page page, CrawlDatums next) {
  //当前页面的depth为x，则从当前页面解析的后续任务的depth为x+1
        int depth;
        //如果在添加种子时忘记添加depth信息，可以通过这种方式保证程序不出错
        if(page.meta("depth")==null){
            depth=1;
        }else{
            depth=Integer.valueOf(page.meta("depth"));
        }
        //depth++;
        for(CrawlDatum datum:next){
            datum.meta("depth", 1+"");
        }
    }
    

    
    public static void main(String[] args)  {
        DepthCrawler crawler=new DepthCrawler("depth_crawler", true);
        /*for(int i=1;i<=5;i++){
            crawler.addSeed(new CrawlDatum("http://search.ccgp.gov.cn/dataB.jsp?searchtype=1&page_index="+i+"&start_time=2016%3A11%3A29&end_time=2016%3A12%3A06&timeType=2&searchchannel=0&dbselect=bidx&kw=&bidSort=0&pinMu=0&bidType=0&buyerName=&projectId=&displayZone=&zoneId=&agentName=")
                    .meta("depth", "1"));
        }*/
        crawler.addSeed(new CrawlDatum("http://search.ccgp.gov.cn/dataB.jsp?searchtype=1&page_index=1&start_time=2016%3A11%3A29&end_time=2016%3A12%3A06&timeType=2&searchchannel=0&dbselect=bidx&kw=&bidSort=0&pinMu=0&bidType=0&buyerName=&projectId=&displayZone=&zoneId=&agentName="));
        /*正则规则用于控制爬虫自动解析出的链接，用户手动添加的链接，例如添加的种子、或
          在visit方法中添加到next中的链接并不会参与正则过滤*/
        /*自动爬取类似"http://news.hfut.edu.cn/show-xxxxxxhtml"的链接*/
        crawler.addRegex("http://http://www.ccgp.gov.cn/cggg/dfgg/zbgg/.*html");
        /*不要爬取jpg|png|gif*/
        crawler.addRegex("-.*\\.(jpg|png|gif).*");
        /*不要爬取包含"#"的链接*/
        crawler.addRegex("-.*#.*");
        
        crawler.setTopN(5);
        
        try {
			crawler.start(2);
		} catch (Exception e) {
			System.err.println("depth crawler main crawler.start");
			
		}
    }

}
