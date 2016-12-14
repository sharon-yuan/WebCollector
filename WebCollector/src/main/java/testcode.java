

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


import cn.edu.hfut.dmic.webcollector.util.FileIO;

public class testcode {
	public static void main(String[]args){
		String dochtml=FileIO.readfromFile("E:/data/china/links/1");
		Document document=Jsoup.parse(dochtml);
		//System.err.println(document.select("a[href]"));
		
		//System.out.println(a);
		//System.err.println("href"+document.html().length()+);
	}

}
