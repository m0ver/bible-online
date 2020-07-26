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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Document;
import org.tinystruct.dom.Element;
import org.tinystruct.handle.Reforward;
import org.tinystruct.mail.SimpleMail;
import org.tinystruct.system.util.ActivationKey;
import org.tinystruct.system.util.URLResourceLoader;

import custom.objects.User;
import custom.objects.bible;
import custom.objects.report;
import custom.objects.vocabulary;

public class sender extends AbstractApplication {
	private HttpServletRequest request;
	private Reforward reforward;
	private HttpServletResponse response;
	private User user;

	public boolean send() {
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		if (this.request.getParameter("id") == null
				|| this.request.getParameter("text") == null
				|| this.request.getParameter("text").trim().length() == 0) {
			return false;
		}

		report report = new report();
		report.setBibleId(this.request.getParameter("id"));
		report.setLanguageId(0);
		report.setUserId("-");
		report.setStatus(0);
		report.setUpdatedContent(this.request.getParameter("text"));

		report.setModifiedTime(new Date());
		try {
			report.append();
			
			bible bible=new bible();
			if(this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
				bible.setTableName("NIV");
			}
			else if(this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
				bible.setTableName("ESV");
			}
			else {
				bible.setTableName(this.getLocale().toString());
			}
			
			bible.setId(report.getBibleId());
			bible.findOneById();
			
			SimpleMail mail = new SimpleMail();
			mail.setFrom(this.getProperty("mail.default.from").toString());
			//你好！有一个用户发来的经文更新请求!<br />原文是：%s，建议更新为：%s <br/> --- <br />请点击此链接进行确认！(或复制此地址到浏览器访问):<br /> %s <br /><br /> InGod.asia工作小组
			String body = String.format(
					this.getProperty("mail.report.content"), bible.getContent(),report.getUpdatedContent(),
					this.context.getAttribute("HTTP_HOST")+"services/bible/update/"+report.getId());
			mail.setSubject(this.getProperty("mail.report.title"));
			mail.setBody(body);
			mail.addTo("moverinfo@gmail.com");

			mail.send();

		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}

	public String invite() throws ApplicationException {
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.user = (User) session.getAttribute("usr");
		}
		else return "false";
		
		String mailto = "moverinfo@gmail.com";
		if (this.request.getParameter("mailto") == null
				|| this.request.getParameter("mailto").trim().length() == 0) {
			return "false";
		} else
			mailto = this.request.getParameter("mailto");

		String[] addresses = mailto.split(";");

		for (int i = 0; i < addresses.length; i++)
			if (addresses[i].indexOf('@') < 1
					|| addresses[i].indexOf('@') >= addresses[i]
							.lastIndexOf('.') + 1) {
				return "invalid";
			}

		SimpleMail mail = new SimpleMail();
		mail.setFrom(this.getProperty("mail.default.from"));

		ActivationKey key = new ActivationKey();
		String randomKey = key.getRandomCode();

		String body = String.format(
				this.getProperty("mail.invitation.content"), this
						.getProperty("application.title"),
				this.getLink("user/register")+"/" + randomKey);
		mail.setSubject(this.getProperty("mail.invitation.title"));
		mail.setBody(body);
		mail.setTo(mailto);

		if (!mail.send()) {
			return "error";
		}

		return "true";
	}

	
	public boolean update(String id) throws ApplicationException{
		report report = new report();
		report.setId(id);
		report.findOneById();
		
		bible bible=new bible();
		
		if(this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
			bible.setTableName("NIV");
		}
		else if(this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
			bible.setTableName("ESV");
		}
		else {
			bible.setTableName(this.getLocale().toString());
		}
		
		bible.setId(report.getBibleId());
		bible.setContent(report.getUpdatedContent());
		
		report.setStatus(1);

		if(bible.update() && report.update()) {
			this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
			this.response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");
	
			bible.findOneById();
			
			this.reforward=new Reforward(this.request,this.response);
			this.reforward.setDefault(this.getLink("bible")+"/"+bible.getBookId()+"/"+bible.getChapterId()+"/"+bible.getPartId());
			this.reforward.forward();
			return true;
		}
		
		return false;
	}

	public void init() {
		// TODO Auto-generated method stub
		this.setAction("friends/invite", "invite");
		this.setAction("services/report", "send");
		this.setAction("services/bible/update", "update");
		this.setAction("services/getword", "getWord");
		this.setAction("services/translate", "getTranslate");
		this.setAction("services/saveword", "saveWord");
		this.setAction("services/getwords", "getAllWords");
		this.setAction("services/deleteword", "deleteWord");
	}
	
	public String getWord(String word) throws MalformedURLException, ApplicationException{
        String url = "http://dict.youdao.com/fsearch?client=deskdict&keyfrom=chrome.extension&q="+word+"&pos=-1&doctype=xml&vendor=unknown&appVer=3.1.17.4208&le=eng";

		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.user = (User) session.getAttribute("usr");
			Document doc=new Document();
			doc.load(new URL(url));
			Element document = doc.getRoot();
			if( document.getChildNodes().size() < 3) throw new ApplicationException("The dictionary resource is temporarily unavailable");
			
			vocabulary vocabulary = new vocabulary();
			vocabulary.setUserId(this.user.getId());
			vocabulary.setDate(new Date());
			vocabulary.setReferenceLink(this.request.getParameter("referer").toString());

			List<Element> phrase = document.getElementsByTagName("return-phrase");
			if(phrase.size()>0)
			{
				vocabulary.setWord(phrase.get(0).getData());
			}
			
			List<Element> phonetic_symbol = document.getElementsByTagName("phonetic-symbol");
			if(phonetic_symbol.size()>0) {
				vocabulary.setPhoneticSymbol(phonetic_symbol.get(0).getData());
			}
			
			List<Element> custom_translation = document.getElementsByTagName("custom-translation");
			if(custom_translation.size()>0) {
				StringBuffer buff=new StringBuffer();
	
				Iterator<Element> citerator = custom_translation.get(0).getElementsByTagName("translation").iterator();
				while(citerator.hasNext()) {
					if(buff.length()>0) buff.append("\r\n");
					buff.append(citerator.next().getElementsByTagName("content").get(0).getData());
				}
				vocabulary.setInterpretation(buff.toString());
			}
			
			Table words = vocabulary.findWith("WHERE word=? and user_id=?", new Object[]{vocabulary.getWord(),this.user.getId()});
			if(words.isEmpty()){
				vocabulary.append();
			}
			else {
				vocabulary.setData(words.get(0));
				vocabulary.setDate(new Date());
				vocabulary.update();
			}
	
			return document.toString();
		}
		
		URLResourceLoader loader=new URLResourceLoader(new URL(url));
		
		return loader.getContent().toString();
	}
	
	public void deleteWord(String id) throws ApplicationException {
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.user = (User) session.getAttribute("usr");
			vocabulary vocabulary = new vocabulary();
			vocabulary.setId(id);
			
			vocabulary.delete();
			
			this.response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");
			
			this.reforward=new Reforward(this.request,this.response);
			this.reforward.setDefault(this.getLink("dashboard"));
			this.reforward.forward();
		}
		else throw new ApplicationException("Permission Denied");
	}
	
	public Object getAllWords(){
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpSession session = request.getSession();
		if(session.getAttribute("usr")!=null) {
			this.user = (User) session.getAttribute("usr");
			StringBuffer buffer=new StringBuffer();
			buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			buffer.append("<list userId=\""+this.user.getId()+"\">");
			vocabulary vocabulary = new vocabulary();
			try {
				Table list = vocabulary.findWith("WHERE user_id=? order by date desc limit 0,7", new Object[]{this.user.getId()});
				Iterator<Row> rows = list.iterator();
				while(rows.hasNext()) {
					vocabulary.setData(rows.next());
					
					buffer.append("<item word=\"").append(vocabulary.getWord()).append("\" phoneticSymbol=\"").append(vocabulary.getPhoneticSymbol()).append("\">").append(vocabulary.getInterpretation()).append("</item>");
				}
			
			} catch (ApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer.append("</list>");
			
			return buffer;
		}
		
		return null;
	}

	public StringBuffer getTranslate(String words) throws MalformedURLException, ApplicationException{
        String url = "http://fanyi.youdao.com/translate?client=deskdict&keyfrom=chrome.extension&xmlVersion=1.1&dogVersion=1.0&ue=utf8&i="+(words)+"&doctype=xml";

		URLResourceLoader loader=new URLResourceLoader(new URL(url));
		
		return loader.getContent();
	}
	
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

}