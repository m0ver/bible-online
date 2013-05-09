/*******************************************************************************
 * Copyright  (c) 2013 Mover Zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package custom.application;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.handle.Reforward;
import org.tinystruct.handle.Report;
import org.tinystruct.system.util.StringUtilities;
import org.tinystruct.system.util.ValidateCode;

import custom.objects.User;

public class login extends AbstractApplication {
	private passport passport;
	private User usr;

	public Object validate() 
	{
		HttpServletRequest request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpServletResponse response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");
		
		Cookie cookie=StringUtilities.getCookieByName(request.getCookies(), "username");
		if(cookie!=null)
		{
			this.setVariable("username",cookie.getValue());
			String user_field=cookie.getValue()+"<input class=\"text\" id=\"username\" name=\"username\" type=\"hidden\" value=\""+cookie.getValue()+"\"/>  <a href=\"javascript:void(0)\" onclick=\"restoreField()\">[%login.user.change%]</a>";

			this.setVariable("user_field", user_field);
		}
		else
		{
			this.setVariable("username", "");
			this.setVariable("user_field", "<input class=\"text\" id=\"username\" name=\"username\" type=\"text\" value=\"\"/>");
		}

		this.setText("login.tips.text",this.getLink("bible"));

		try 
		{
			Reforward reforward = new Reforward(request, response);

			if(request.getMethod().equalsIgnoreCase("post")) {
				this.passport=new passport(request,response, "waslogined");
				if(this.passport.login())
				{
					reforward.forward();
				}
			}
			
			this.setVariable("from", reforward.getFromURL());
		} catch (ApplicationException e) {
			this.setVariable("error", "<div class=\"error\">"+e.getRootCause().getMessage()+"</div>");
		}
		
		this.setVariable("action", this.config.get("default.base_url")+this.context.getAttribute("HTTP_REQUEST_ACTION").toString());
		
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.usr = (User) session.getAttribute("usr");
			
			this.setVariable("user.status","");
			this.setVariable("user.profile","<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"+this.usr.getEmail()+"</a>");
		}
		else {
			this.setVariable("user.status","<a href=\""+this.getLink("user/login")+"\">"+this.getProperty("page.login.caption")+"</a>");
			this.setVariable("user.profile","");
		}
		
		return this;
	}
	
	public void logout()
	{
		HttpServletRequest request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpServletResponse response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");

		try {
			this.passport=new passport(request,response, "waslogined");
			this.passport.logout();
			
			if(request.getCookies()!=null)
			{
				Cookie[] cookies=request.getCookies();
				int i=0;
				Cookie cookie;
				while(cookies.length>i)
				{
					cookie = cookies[i];
					cookie.setMaxAge(0);
					cookie.setValue("");
					response.addCookie(cookie);
					i++;
				}
			}
			
			Reforward reforward = new Reforward(request, response);

			reforward.setDefault(this.getLink(this.context.getAttribute("default.login.page").toString())+"&from="+reforward.getFromURL());
			reforward.forward();
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.setAction("user/login", "validate");
		this.setAction("user/logout", "logout");
		this.setAction("validator/code", "toImage");
		
		this.setVariable("error", "");
		this.setVariable("service", "");
		this.setVariable("application.summary","");
		
		this.setText("login");
		this.setText("login.user.caption");
		this.setText("login.password.caption");
		this.setText("login.verifycode.caption");
		this.setText("login.remember.caption");
		this.setText("login.submit.caption");
		this.setText("login.username.invalid");
		this.setText("login.password.invalid");
		this.setText("login.authorized.invalid");
		this.setText("login.user.change");
		
		this.setText("navigator.login.caption");
		this.setText("footer.forgot");
		this.setText("page.login.title");
		this.setText("page.welcome.caption");
		this.setText("page.language-setting.title");
		this.setText("page.logout.caption");

		this.setText("application.title");
		this.setText("application.language.name");

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
		
		String username="";
		if(this.getVariable("username")!=null) {
			username = String.valueOf(this.getVariable("username").getValue());
		}

		this.setText("page.welcome.hello", (username == null || username.trim()
				.length() == 0) ? "" : username + "，");		
	}
	
	public void toImage(){
		
		HttpServletRequest request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpServletResponse response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");
		
		response.setHeader("Pragma","No-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires", 0);
		
		try
		{
			ValidateCode code=new ValidateCode(request);
			code.toImage(response);
		}
		catch(java.io.IOException io)
		{
			Report.getInstance().print(io.getMessage());
		}
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

}