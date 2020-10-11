package custom.application;

import custom.objects.User;
import custom.objects.book;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Cache;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;

public class index extends AbstractApplication {
	private User usr;

	public void init(){
		this.setText("application.keywords");
		this.setText("application.description");
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
		this.setText("page.reading.title");
		this.setText("holy.book.forward");
		this.setText("holy.book.previous");
		this.setText("holy.book.next");
		this.setText("holy.bible");
		this.setText("holy.bible.old-testament");
		this.setText("holy.bible.new-testament");
		this.setText("holy.bible.directory");
		this.setText("holy.book.find-and-reading");
		this.setText("holy.book.tools");
		this.setText("holy.bible.version");
		
		this.setText("footer.report-a-site-bug");
		this.setText("footer.privacy");
		this.setText("footer.register");
		this.setText("footer.api");
		this.setText("footer.updates-rss");
		this.setText("holy.book.select");
		
		this.setText("search.confirm.caption");
		this.setText("search.submit.caption");

		this.setText("invite.confirm.caption");
		this.setText("invite.submit.caption");
		this.setText("invite.email.default.tips");

		this.setText("subscribe.plan");
		this.setText("subscribe.bible.plan");
		this.setText("subscribe.article.plan");
		this.setText("subscribe.submit.caption");
		this.setText("subscribe.email.caption");
		this.setText("subscribe.email.default.tips");
		
		this.setText("user.lastlogin.caption");
		
		this.setText("holy.bible.introduction");
		this.setText("holy.bible.introduction1");

		this.setText("holy.bible.download");
		this.setText("holy.bible.chinese.download");
		
		this.setVariable("TEMPLATES_DIR", "/themes", false);
		this.setVariable("keyword",this.getVariable("keyword")==null?"":this.getVariable("keyword").getValue().toString());
		this.setVariable("metas", "");

		String username="";
		if(this.getVariable("username")!=null) {
			username = String.valueOf(this.getVariable("username").getValue());
		}
		
		this.setText("page.welcome.hello", (username == null || username.trim()
				.length() == 0) ? "" : username + "，");
		
		this.setAction("default", "start");
		
	}
	
	public Object start(){
		HttpServletRequest request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		this.setVariable("action", String.valueOf(this.context.getAttribute("HTTP_HOST"))+this.context.getAttribute("REQUEST_ACTION").toString());
		this.setVariable("base_url", String.valueOf(this.context.getAttribute("HTTP_HOST")));
		
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.usr = (User) session.getAttribute("usr");
			
			this.setVariable("user.status","");
			this.setVariable("user.profile","<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"+this.usr.getEmail()+"</a>");
			this.setVariable("scripts","$.ajax({url:\""+this.getLink("services/getwords")+"\",dataType:\"xml\",type:'GET'}).success(function(data){data=wordsXML(data);ldialog.show(data);});");
		}
		else {
			this.setVariable("user.status","<a href=\""+this.getLink("user/login")+"\">"+this.getProperty("page.login.caption")+"</a>");
			this.setVariable("user.profile","");
			this.setVariable("scripts","");
		}
		
		try {
			this.menu();
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return this;
	}

	public Object menu() throws ApplicationException {
		Cache data = Cache.getInstance();
		
		Object menu;
		Table list = null;
		book book = new book();
		String _locale = this.getLocale().toString();
		if((menu = data.get("cache-menu"+_locale)) == null) {
			try {
				list=book.findWith("WHERE language=? order by book_id", new Object[]{_locale});
				data.set("cache-menu"+_locale, list);
			} catch (ApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			list = (Table) menu;
		}
		
		int i=0;
		
		Element ul = new Element("ol"), ul1 = new Element("ol"), li, a;
		Iterator<Row> item = list.iterator();
		String bookName;
		while (item.hasNext()) {
			book.setData(item.next());

			li = new Element("li");
			a = new Element("a");

			bookName = book.getBookName();

			a.setAttribute("href", this.context.getAttribute("HTTP_HOST") + bookName);
			a.setAttribute("title", bookName);
			a.setData(bookName);
			li.addElement(a);

			if (i++ < 39)
				ul.addElement(li);
			else
				ul1.addElement(li);
		}
		
		ul.setAttribute("class","menu");
		ul1.setAttribute("class", "menu");
		ul1.setAttribute("start", "40");

		this.setVariable("old-testament", ul.toString(), true);
		this.setVariable("new-testament", ul1.toString(), true);

		return this;
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}
}
