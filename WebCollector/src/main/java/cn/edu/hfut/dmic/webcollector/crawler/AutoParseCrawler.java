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


import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hu
 */
public abstract class AutoParseCrawler extends Crawler implements Executor, Visitor, Requester {

    public static final Logger LOG = LoggerFactory.getLogger(AutoParseCrawler.class);

    /**
     * 是否自动抽取符合正则的链接并加入后续任务
     */
    protected boolean autoParse = true;

    protected Visitor visitor;
    protected Requester requester;
public static Proxys proxys=new Proxys();
    public AutoParseCrawler(boolean autoParse) {
        this.autoParse = autoParse;
        this.visitor = this;
        this.requester = this;
        this.executor = this;


		List<String[]> proxyList=cn.edu.hfut.dmic.webcollector.net.proxyController.readerProxyFromDir();
		System.out.println(proxyList.get(0).length);
		for(String[]tempProxy:proxyList){
			System.out.println(tempProxy[0]+" "+tempProxy[1]);
			proxys.add(tempProxy[0],Integer.valueOf(tempProxy[1]));
		}
		
	
    }

    @Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum,proxys.nextRandom());
      
    	 
        
        return request.getResponse();
    }
public static void badProxy(java.net.Proxy proxy){
	System.err.println(proxy);
	proxys.remove(proxy.toString());
	
}
    /**
     * URL正则约束
     */
    protected RegexRule regexRule = new RegexRule();

    @Override
    public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
    	if(requester==null){System.err.println("AutoParseCrawler.execute requester == null");next.add(datum);throw new Exception();}
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
        afterParse(page, next);
    }

    protected void afterParse(Page page, CrawlDatums next) {
if(page!=null)
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
     * @param urlRegex URL正则约束
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
     * @param autoParse 是否自动抽取符合正则的链接并加入后续任务
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
     * @param regexRule 正则规则
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
     * @param visitor Visitor
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
