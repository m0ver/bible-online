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

import custom.objects.User;
import custom.objects.suggestion;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.mail.SimpleMail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.logging.Logger;

public class suggest extends AbstractApplication
{
    private Logger logger = Logger.getLogger("suggest.class");
	private HttpServletRequest request;
	private User usr;

	@Override
	public void init() {

		this.setAction("suggestion", "send");
		this.setAction("suggestion/post", "post");
		
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
		
		this.setText("page.suggestion.title");
		
		this.setText("suggestion.email.address");
		this.setText("suggestion.content.text");
		this.setText("suggestion.button.ok");
		this.setText("suggestion.button.cancel");
		this.setText("suggestion.content.invalid");
		this.setText("suggestion.email.invalid");
		this.setText("suggestion.send.success");
		this.setText("suggestion.send.failure");
	}
	
    public Object send() 
    {
    	StringBuffer html=new StringBuffer();

    	html.append("<div>\r\n");
    	html.append("<form id=\"poster\" action=\""+this.getLink("suggestion/post")+"\" method=\"post\">");
    	html.append("<ul>\r\n");
    	html.append("<li><label>"+this.getProperty("suggestion.email.address")+"<br /><input type=\"text\" value=\"\" name=\"iemail\" id=\"iemail\" class=\"text\"/></label></li>\r\n");
    	html.append("<li><label>"+this.getProperty("suggestion.content.text")+"<br /><textarea name=\"content\" id=\"content\" cols=\"45\" rows=\"8\"></textarea></label></li>\r\n");
    	html.append("<li><label><input type=\"submit\" class=\"button-secondary\" value=\""+this.getProperty("suggestion.button.ok")+"\"/> </label><input type=\"reset\" class=\"button-secondary\" value=\""+this.getProperty("suggestion.button.cancel")+" \" onclick=\"history.back()\"/></li>\r\n");
    	html.append("</ul>\r\n");
		html.append("</form>\r\n");
		html.append("</div>\r\n");
		html.append("<div style=\"clear:both;height:10px\"></div>\r\n");
		
		this.setVariable("error", "");
		this.setVariable("value", html.toString());
		this.setVariable("action", this.config.get("default.base_url")+this.context.getAttribute("REQUEST_ACTION").toString());
		
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");

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
    
    public Object post() throws ApplicationException
    {
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");

    	suggestion suggestion=new suggestion();
    	
    	if (this.request.getParameter("content") == null || this.request.getParameter("content").trim().length() <= 0)
        {
    		this.setVariable("error", "<div class=\"error\">"+this.getProperty("suggestion.content.invalid")+"</div>");
    		
    		return this;
        }
    	
    	if (this.request.getParameter("iemail") == null || this.request.getParameter("iemail").trim().length() <= 0)
        {
    		this.setVariable("error", "<div class=\"error\">"+this.getProperty("suggestion.email.invalid")+"</div>");
    		
    		return this;    		
        }
    	
    	String content=this.request.getParameter("content");
		String email=this.request.getParameter("iemail");
    	suggestion.setContent(content);
    	suggestion.setEmail(email);
    	suggestion.setIP(this.request.getRemoteAddr());
    	suggestion.setPostDate(new Date());
    	suggestion.setTitle("Suggestion");
    	try {
			suggestion.append();
			
			SimpleMail mail = new SimpleMail();
			mail.setFrom(this.getProperty("mail.default.from").toString());
			//你好！有一个用户发来的经文更新请求!<br />原文是：%s，建议更新为：%s <br/> --- <br />请点击此链接进行确认！(或复制此地址到浏览器访问):<br /> %s <br /><br /> InGod.asia工作小组
			String body = String.format(
					this.getProperty("mail.suggestion.content"),email,content);
			mail.setSubject(this.getProperty("mail.suggestion.title"));
			mail.setBody(body);
			mail.addTo("moverinfo@gmail.com");
			mail.send();
			
			this.setVariable("error", "<div class=\"info\">"+this.getProperty("suggestion.send.success")+"</div>");
		} catch (ApplicationException e) {
    		logger.severe(e.getMessage());
			this.setVariable("error", "<div class=\"error\">"+this.getProperty("suggestion.send.failure")+"</div>");
		}
		this.setVariable("action", String.valueOf(this.context.getAttribute("HTTP_HOST"))+this.context.getAttribute("REQUEST_ACTION").toString());
		
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

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}
}
