package custom.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;

import custom.objects.User;

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
		
		this.setText("holy.bible.download");
		this.setText("holy.bible.chinese.download");
		
		this.setVariable("TEMPLATES_DIR", "/themes");
		this.setVariable("language",this.config.get("language").toString());
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
		this.setVariable("action", this.config.get("default.base_url")+this.context.getAttribute("HTTP_REQUEST_ACTION").toString());
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
		return this;
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}
}
