package custom.application;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.system.util.TextFileLoader;

public class help extends AbstractApplication {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.setAction("help", "privacy");
		this.setAction("help/condition", "condition");
		this.setAction("sitemap.xml", "sitemap");
		
		this.setText("page.condition.title");
		this.setText("application.title");
		this.setText("application.language.name");

		this.setText("page.welcome.caption");
		this.setText("page.language-setting.title");
		this.setText("page.logout.caption");

		this.setText("navigator.home.caption");
		this.setText("navigator.bible.caption");
		this.setText("navigator.video.caption");
		this.setText("navigator.document.caption");
		this.setText("navigator.reader.caption");
		this.setText("navigator.controller.caption");
		this.setText("navigator.help.caption");

		this.setText("footer.report-a-site-bug");
		this.setText("footer.privacy");
		this.setText("footer.register");
		this.setText("footer.api");
		this.setText("footer.updates-rss");
	}
	
	public void privacy() throws ApplicationException{
		
		InputStream in = help.class.getClassLoader().getResourceAsStream("themes/privacy.view");
		TextFileLoader loader=new TextFileLoader(in);
		
		this.setBuffer(loader.getContent().toString());
	}
	
	public void condition() throws ApplicationException{
		InputStream in = help.class.getClassLoader().getResourceAsStream("themes/condition.view");
		TextFileLoader loader=new TextFileLoader(in);
		
		this.setBuffer(loader.getContent().toString());
	}
	
	public Object sitemap(){
		Collection<String> paths = this.actions().paths();
		StringBuffer buffer=new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");
		buffer.append("<url>\r\n");
		buffer.append("  <loc>http://ingod.asia/</loc>\r\n");
		buffer.append("  <changefreq>daily</changefreq>\r\n");
		buffer.append("  <priority>1.00</priority>\r\n");
		buffer.append("</url>\r\n");
		
		String path;
		Iterator<String> iterator = paths.iterator();
		while(iterator.hasNext()) {
			path=iterator.next();
			buffer.append("<url>\r\n");
			buffer.append("  <loc>"+this.getLink(path).replace("&", "&amp;")+"</loc>\r\n");
			buffer.append("  <changefreq>daily</changefreq>\r\n");
			buffer.append("  <priority>0.80</priority>\r\n");
			buffer.append("</url>\r\n");
		}
		
		int i=1;
		while(i++<66){
			buffer.append("<url>\r\n");
			buffer.append("  <loc>"+this.getLink("bible").replace("&", "&amp;")+"/"+i+"</loc>\r\n");
			buffer.append("  <changefreq>weekly</changefreq>\r\n");
			buffer.append("  <priority>0.80</priority>\r\n");
			buffer.append("</url>\r\n");
		}
		
		buffer.append("</urlset>");
		
		HttpServletResponse response = (HttpServletResponse) this.context
		.getAttribute("HTTP_RESPONSE");

		response.setContentType("text/xml;charset="
		+ this.config.get("charset"));

		return buffer;
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

}
