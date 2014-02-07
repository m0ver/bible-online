package custom.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;

import custom.objects.User;

public class index extends AbstractApplication {
	private User usr;

	public void init(){
		new lection().init();
		this.setAction("default", "start");
	}
	
	public Object start(){
		HttpServletRequest request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");

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
