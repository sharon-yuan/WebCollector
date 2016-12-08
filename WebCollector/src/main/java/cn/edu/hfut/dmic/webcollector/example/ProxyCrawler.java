package cn.edu.hfut.dmic.webcollector.example;

import java.util.List;

import org.json.JSONObject;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

public class ProxyCrawler extends BreadthCrawler {

    public ProxyCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    public ProxyCrawler(String crawlPath, List<String> seeds, List<String> regexs) {
		// TODO Auto-generated constructor stub
    	  super(crawlPath, true);
	}

	@Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum.getUrl());
        
        request.setMethod(crawlDatum.meta("method"));
        String outputData=crawlDatum.meta("outputData");
        if(outputData!=null){
            request.setOutputData(outputData.getBytes("utf-8"));
        }
      
        return request.getResponse();
        /*
        //通过下面方式可以设置Cookie、User-Agent等http请求头信息
        request.setCookie("xxxxxxxxxxxxxx");
        request.setUserAgent("WebCollector");
        request.addHeader("xxx", "xxxxxxxxx");
        */
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String jsonStr = page.getHtml();
        JSONObject json = new JSONObject(jsonStr);
        System.out.println("JSON信息：" + json);
    }

    /**
     * 假设我们要爬取三个链接 1)http://www.A.com/index.php 需要POST，并且需要附带数据id=a
     * 2)http://www.B.com/index.php?id=b 需要POST，不需要附带数据 3)http://www.C.com/
     * 需要GET
     *
     * @param args 参数
     * @throws Exception 异常
     */
    public static void main(String[] args) throws Exception {

        DemoPostCrawler crawler = new DemoPostCrawler("json_crawler", true);
        crawler.addSeed(new CrawlDatum("http://www.A.com/index.php")
                .meta("method", "POST")
                .meta("outputData", "id=a"));
        crawler.addSeed(new CrawlDatum("http://www.B.com/index.php")
                .meta("method", "POST"));
        crawler.addSeed(new CrawlDatum("http://www.C.com/index.php")
                .meta("method", "GET"));

        crawler.start(1);
    }



}
