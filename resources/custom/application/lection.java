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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Cache;
import org.tinystruct.data.component.Field;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.datatype.DataVariable;
import org.tinystruct.dom.Element;
import org.tinystruct.system.Authentication;
import org.tinystruct.system.util.Base64;

import custom.objects.User;
import custom.objects.bible;
import custom.objects.book;


public class lection extends AbstractApplication {
	private int bookid;
	private int chapterid;
	private int partid;

	private int max_chapter = 0;
	private int lastchapterid;
	private int nextchapterid;
	private HttpServletResponse response;
	private book book;
	private HttpServletRequest request;
	private User usr;
	private Cache data=Cache.getInstance();

	@Override
	public void init() {
		this.setAction("bible", "read");
		this.setAction("bible/api", "api");
		this.setAction("bible/feed", "feed");
		
		this.book=new book();
		
		this.data=Cache.getInstance();
		
		try {
			Table list=book.findWith("WHERE language=?", new Object[]{this.getLocale().toString()});
			
			Iterator<Row> item = list.iterator();
			String bookName;
			while(item.hasNext()){
				book.setData(item.next());
				
				bookName = book.getBookName();
				this.setAction(bookName, "viewByName");
				this.data.set(bookName, book.getBookId());
			}
			
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		this.setAction("bible/version", "version");
//		this.setAction("bible/version=", "setVersion");
//		this.setAction("bible/config", "getConfiguration");
		
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
		this.setVariable("keyword",this.getVariable("keyword")==null?"":this.getVariable("keyword").getValue().toString());
		this.setVariable("metas", "");

		String username="";
		if(this.getVariable("username")!=null) {
			username = String.valueOf(this.getVariable("username").getValue());
		}
		
		this.setText("page.welcome.hello", (username == null || username.trim()
				.length() == 0) ? "" : username + "，");
	}
	
	@Override
	public String version() {
		return null;
	}
	
	public Object viewByName() throws ApplicationException{
		return this.viewByName(1);
	}
	
	public Object viewByName(int chapterId) throws ApplicationException {
		return this.viewByName(chapterId, 0);
	}
	
	public Object viewByName(int chapterId, int partId) throws ApplicationException {
		String bookName = (String) this.context.getAttribute("REQUEST_ACTION");
		
		if(bookName.indexOf('/')!=-1)
			bookName = bookName.split("/")[0];
		
		return this.read(Integer.valueOf((this.data.get(bookName).toString())), chapterId, partId);
	}
	
	public Object read() throws ApplicationException {
		return this.read(1);
	}
	
	public Object read(int bookid) throws ApplicationException {
		return this.read(bookid, 1);
	}
	
	public Object read(int bookid,int chapterid) throws ApplicationException {
		return this.read(bookid, chapterid, 0);
	}
	
	public Object read(int bookid, int chapterid, int partid) throws ApplicationException {
		if(bookid==0) bookid=1;
		if(chapterid==0) chapterid=1;

		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		this.setVariable("action", this.config.get("default.base_url")+this.context.getAttribute("REQUEST_ACTION").toString());
		this.setVariable("base_url", String.valueOf(this.context.getAttribute("HTTP_HOST")));
		
		HttpSession session = this.request.getSession();
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

		this.bookid = bookid;
		this.chapterid = chapterid;
		this.partid = partid;
		
		book = book==null?new book():this.book;
		try {
			String lang = this.getLocale().toString();
			if(lang.equalsIgnoreCase("en_GB")) {
				lang = "en_US";
			}
			
			Table table = book.findWith("WHERE book_id=? and language=?",
					new Object[] { this.bookid, lang });
			
			if (table.size() > 0) {
				Row row = table.get(0);
				book.setData(row);
			}
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		
		this.setVariable(new DataVariable("book",book));
		
		String condition = "book_id=" + this.bookid + " and chapter_id="
					+ this.chapterid;
		
		String where = "WHERE " + condition + " order by part_id";
		
		bible bible = new bible();
		if(this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
			bible.setTableName("NIV");
			this.setVariable("language.switch", "<a href=\"?q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">中文</a> <a href=\"?lang=en-GB&q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">ESV</a>");
		}
		else if(this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
			bible.setTableName("ESV");
			this.setVariable("language.switch", "<a href=\"?q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">中文</a> <a href=\"?lang=en-US&q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">NIV</a>");
		}
		else {
			bible.setTableName(this.getLocale().toString());
			this.setVariable("language.switch", "<a href=\"?lang=en-GB&q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">ESV</a> <a href=\"?lang=en-US&q=bible/"+this.bookid+"/"+this.chapterid+"/"+this.partid+"#up\">NIV</a>");
		}
		
		Table vtable = bible.findWith(where, new Object[] {});
		
		this.max_chapter = bible.setRequestFields(
				"max(chapter_id) as max_chapter").findWith("WHERE book_id=?",
				new Object[] { this.bookid }).get(0).get(0).get("max_chapter")
				.intValue();
		this.lastchapterid = this.chapterid - 1 <= 0 ? 1 : this.chapterid - 1;
		this.nextchapterid = this.chapterid + 1 > this.max_chapter ? this.max_chapter
				: this.chapterid + 1;
		
		this.setVariable("chapterid", String.valueOf(this.chapterid));
		this.setVariable("partid", String.valueOf(this.partid));
		this.setVariable("maxchapter", String.valueOf(this.max_chapter));
		this.setVariable("lastchapter", String.valueOf(this.lastchapterid));
		this.setVariable("nextchapter", String.valueOf(this.nextchapterid));
		
		this.setText("holy.book.info", book.getBookName(),this.chapterid,this.max_chapter);
		
		int count = vtable.size();
		StringBuffer left_column = new StringBuffer(), right_column = new StringBuffer();
		String line, line1;
		
		if (count > 0) {
			int n = count / 2, i;
		
			left_column.append("<ol>");
			right_column.append("<ol start=\"" + (n + 1) + "\">");
		
			bible bible1 = new bible();
			for (i = 0; i < n; i++) {
				Row item = vtable.get(i), item1 = vtable.get(i + n);
				bible.setData(item);
				bible1.setData(item1);
		
				line = bible.getContent();
		
				if(line!=null) {
				if (i == 0 && line.trim().length()>0)
					line = "<span class='firstletter'>" + line.substring(0, 1)
							+ "</span>" + line.substring(1, line.length());
		
					line = line.replaceAll("\n\n", "<br />");
					left_column
							.append("<li"
									+ (this.partid == bible.getPartId() ? " class=\"selected\""
											: "")
									+ "><a class=\"sup\" onmousedown=\"rightMenu.show(event,'"
									+ bible.getId() + "')\">" + bible.getPartId()
									+ "</a>" + line + "</li>");
				}
				
				line1 = bible1.getContent();
				line1 = line1.replaceAll("\n\n", "<br />");
				right_column
						.append("<li"
								+ (this.partid == bible1.getPartId() ? " class=\"selected\""
										: "")
								+ "><a class=\"sup\" onmousedown=\"rightMenu.show(event,'"
								+ bible1.getId() + "')\">" + bible1.getPartId()
								+ "</a>" + line1 + "</li>");
			}
		
			if ((i + n) < count) {
				bible.setData(vtable.get(i + n));
				right_column
						.append("<li"
								+ (this.partid == bible.getPartId() ? " class=\"selected\""
										: "")
								+ "><a class=\"sup\" onmousedown=\"rightMenu.show(event,'"
								+ bible.getId() + "')\">" + bible.getPartId()
								+ "</a>" + bible.getContent() + "</li>");
			}
		
			left_column.append("</ol>");
			right_column.append("</ol>");
		} else {
			left_column.append("暂时没有任何内容");
		}
		
		this.setVariable("left_column", left_column.toString());
		this.setVariable("right_column", right_column.toString());
		
		return this;
	}
	
	public Object menu() throws ApplicationException {

		int i=0;
		Element ul = new Element("ol"), ul1 = new Element("ol"), li, a;

		while (i++ < 66) {

			li = new Element("li");
			a = new Element("a");

			a.setAttribute("href", this.getContext().getAttribute("HTTP_HOST") + "bible/"
							+ i);
			
//			a.setData(this.data);
			li.addElement(a);

			if (bookid < 40)
				ul.addElement(li);
			else
				ul1.addElement(li);
		}

		ul1.setAttribute("start", "40");

		this.setVariable("old-testament", ul.toString());
		this.setVariable("new-testament", ul1.toString());

		return this;
	}
	
	public String feed() throws ApplicationException {
		return this.feed(1, 1);
	}
	
	public String feed(int bookId, int chapterId) throws ApplicationException {
		Element element = new Element();

		Element root = (Element) element.clone();
		root.setName("rss");
		root.setAttribute("version", "2.0");
		root.setAttribute("xmlns:content",
				"http://purl.org/rss/1.0/modules/content/");
		root.setAttribute("xmlns:wfw", "http://wellformedweb.org/CommentAPI/");
		root.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		root.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
		root.setAttribute("xmlns:sy",
				"http://purl.org/rss/1.0/modules/syndication/");
		root.setAttribute("xmlns:slash",
				"http://purl.org/rss/1.0/modules/slash/");

		Element channel = (Element) element.clone();
		channel.setName("channel");

		Element title = (Element) element.clone();
		title.setName("title");
		title.setData(this.getProperty("application.title"));
		channel.addElement(title);

		Element atom_link = (Element) element.clone();
		atom_link.setName("atom:link");
		atom_link.setAttribute("href", this.getLink("bible").replaceAll("&", "&amp;"));
		atom_link.setAttribute("rel", "self");
		atom_link.setAttribute("type", "application/rss+xml");
		channel.addElement(atom_link);

		Element link = (Element) element.clone();
		link.setName("link");
		link.setData(this.getLink("bible").replaceAll("&", "&amp;"));
		channel.addElement(link);

		Element description = (Element) element.clone();
		description.setName("description");
		description.setData("<![CDATA[ "
				+ this.getProperty("application.description") + " ]]>");
		channel.addElement(description);

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd"),full_format = new SimpleDateFormat("yyyy-MM-dd");
		Element lastBuildDate = (Element) element.clone();
		lastBuildDate.setName("lastBuildDate");
		
		lastBuildDate.setData(full_format.format(date));
		channel.addElement(lastBuildDate);

		Element generator = (Element) element.clone();
		generator.setName("generator");
		generator.setData("g");
		channel.addElement(generator);

		Element language = (Element) element.clone();
		language.setName("language");
		language.setData(this.getLocale().toString());
		channel.addElement(language);

		Element sy_updatePeriod = (Element) element.clone();
		sy_updatePeriod.setName("sy:updatePeriod");
		sy_updatePeriod.setData("daily");
		channel.addElement(sy_updatePeriod);

		Element sy_updateFrequency = (Element) element.clone();
		sy_updateFrequency.setName("sy:updateFrequency");
		sy_updateFrequency.setData("1");
		channel.addElement(sy_updateFrequency);
		
		Table vtable = this.load(bookId, chapterId, 0);

		Element item = new Element("item");
		Element item_title = new Element("title");
		item_title.setData("<![CDATA[" + this.book.getBookName() +" "+ this.chapterid + "]]>");
		item.addElement(item_title);

		Element item_link = new Element("link");
		item_link.setData(this.getLink("bible").replace("&", "&amp;")+"/"+ format.format(new Date()));
		item.addElement(item_link);

		Element item_comments = new Element("comments");
		item_comments.setData("");
		item.addElement(item_comments);

		Element item_pubDate = new Element("pubDate");
		item_pubDate.setData(full_format.format(date));
		item.addElement(item_pubDate);

		Element dc_creator = new Element("dc:creator");
		dc_creator.setData("<![CDATA[" + "Bible System" + "]]>");
		item.addElement(dc_creator);

		Element category = (Element) element.clone();
		category.setName("category");
		category.setData(format.format(date));
		item.addElement(category);

		Element guid = (Element) element.clone();
		guid.setName("guid");
		guid.setAttribute("isPermaLink", "true");
		guid.setData(this.getLink("feed").replace("&", "&amp;")+"/"
				+ format.format(new Date()));
		item.addElement(guid);
		
		// start
		StringBuffer buffer=new StringBuffer();
		String finded;
		buffer.append("<ol style=\"list-style-type: none;\">");
		int count = vtable.size();
		if (count > 0) {
			Field fields;
			for (Enumeration<Row> table = vtable.elements(); table.hasMoreElements();) {
				Row row = table.nextElement();
				Iterator<Field> iterator = row.iterator();
	
				while (iterator.hasNext()) {
					fields = iterator.next();
					finded = fields.get("content").value().toString();
					buffer.append("<li"
								+ (this.partid == fields.get("part_id").intValue() ? " class=\"selected\""
										: "")
								+ "><a class=\"sup\">" + fields.get("part_id").intValue()
								+ "</a>" + finded + "</li>");
				}
			}
			buffer.append("</ol>");
		} else {
			buffer.append("暂时没有任何内容");
		}
		
		Element item_description = (Element) element.clone();
		item_description.setName("description");
		item_description
				.setData("<![CDATA[" + buffer + "]]>");
		
		item.addElement(item_description);
		
		Element content_encoded = (Element) element.clone();
		content_encoded.setName("content:encoded");
		content_encoded.setData("<![CDATA["
				+ buffer.toString() + "]]>");
		item.addElement(content_encoded);

		Element wfw_commentRss = (Element) element.clone();
		wfw_commentRss.setName("wfw:commentRss");
		wfw_commentRss.setData("");
		item.addElement(wfw_commentRss);

		Element slash_comments = (Element) element.clone();
		slash_comments.setName("slash:comments");
		slash_comments.setData("");
		item.addElement(slash_comments);

		channel.addElement(item);

		root.addElement(channel);
		// end

		this.response = (HttpServletResponse) this.context
				.getAttribute("HTTP_RESPONSE");

		this.response.setContentType("text/xml;charset="
				+ this.config.get("charset"));

		StringBuffer xbuffer = new StringBuffer();
		xbuffer.append("<?xml version=\"1.0\" encoding=\""
				+ this.config.get("charset") + "\"?>\r\n");
		xbuffer.append(root);

		return xbuffer.toString();
	}
	
	public Object bible() throws ApplicationException {
		return this.bible(1);
	}
	
	public Object bible(int bookid) throws ApplicationException {
		return this.bible(bookid, 1);
	}
	
	public Object bible(int bookid,int chapterid) throws ApplicationException {
		return this.bible(bookid, chapterid, 0);
	}
	
	public Object bible(int bookid,int chapterid,int partid) throws ApplicationException {
		StringBuffer xml = new StringBuffer();
		String finded = "";

		Table vtable = this.load(bookid, chapterid, partid);

		xml.append("<?xml version=\"1.0\" encoding=\"" + this.context.getAttribute("charset")
				+ "\"?>");
		xml.append("<book id=\"book\" name=\"book\" bookid=\"" + this.bookid
				+ "\" bookname=\"" + this.book.getBookName() + "\" chapterid=\""
				+ this.chapterid + "\" maxchapter=\"" + this.max_chapter
				+ "\" lastchapter=\"" + this.lastchapterid
				+ "\" nextchapter=\"" + this.nextchapterid + "\">\r\n");
		Field fields;
		for (Enumeration<Row> table = vtable.elements(); table
				.hasMoreElements();) {
			Row row = table.nextElement();
			Iterator<Field> iterator = row.iterator();

			while (iterator.hasNext()) {
				fields = iterator.next();
				finded = fields.get("content").value().toString();
				if (this.partid == Integer.parseInt(fields.get("part_id")
						.value().toString())) {
					xml.append("<item uid=\""
							+ fields.get("id").value().toString() + "\" id=\""
							+ fields.get("part_id").value().toString()
							+ "\" selected=\"true\">" + finded + "</item>");
				} else {
					xml.append("<item uid=\""
							+ fields.get("id").value().toString() + "\" id=\""
							+ fields.get("part_id").value().toString()
							+ "\" selected=\"false\">" + finded + "</item>");
				}
			}
		}
		xml.append("</book>");

		return xml.toString();
	}
	
	private Table load(int bookid,int chapterid,int partid) throws ApplicationException {

		this.bookid = bookid;
		this.chapterid = chapterid;
		this.partid = partid;

		book = new book();
		try {
			String lang = this.getLocale().toString();
			if(this.getLocale().toString().equalsIgnoreCase("en_GB")) {
				lang = "en_US";
			}
			
			Table table = book.findWith("WHERE book_id=? and language=?",
					new Object[] { this.bookid, lang });
			
			if (table.size() > 0) {
				Row row = table.get(0);
				book.setData(row);
			}
		} catch (ApplicationException e) {
			e.printStackTrace();
		}

		String condition = "";

		if (this.chapterid == 0) {
			condition = "book_id=" + this.bookid;
		} else {
			condition = "book_id=" + this.bookid + " and chapter_id="
					+ this.chapterid;
		}

		String where = "WHERE " + condition + " order by part_id";

		bible bible = new bible();
		
		if(this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) 
			bible.setTableName("ESV");
		else if(this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) 
			bible.setTableName("NIV");
		else
		bible.setTableName(this.getLocale().toString());
		
		Table vtable = bible.findWith(where, new Object[] {});

		this.max_chapter = bible.setRequestFields(
				"max(chapter_id) as max_chapter").findWith("WHERE book_id=?",
				new Object[] { this.bookid }).get(0).get(0).get("max_chapter")
				.intValue();
		this.lastchapterid = this.chapterid - 1 <= 0 ? 1 : this.chapterid - 1;
		this.nextchapterid = this.chapterid + 1 > this.max_chapter ? this.max_chapter
				: this.chapterid + 1;

		this.setText("holy.book.info", book.getBookName(),this.chapterid,this.max_chapter);

		return vtable;
	}

	public Object api() throws ApplicationException
	{
		boolean valid = false;
		
		HttpServletRequest request=(HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		HttpServletResponse response = (HttpServletResponse) this.context.getAttribute("HTTP_RESPONSE");

		String s = "Basic realm=\"Login for Bible API\"";
		response.setHeader("WWW-Authenticate", s);
		
		// Get the Authorization header, if one was supplied
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				// We only handle HTTP Basic authentication

				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();

					// This example uses sun.misc.* classes.
					// You will need to provide your own
					// if you are not comfortable with that.

					String userPass = new String(Base64.decode(credentials));
					// The decoded string is in the form
					// "userID:password".

					int p = userPass.indexOf(":");
					if (p != -1) {
						final String userID = userPass.substring(0, p);
						final String password = userPass.substring(p + 1);

						// Validate user ID and password
						// and set valid true true if valid.
						// In this example, we simply check
						// that neither field is blank

						if ((!userID.trim().equals(""))
								&& (!password.trim().equals(""))) {
							Authentication context=new Authentication(){
								public boolean status() {
									return userID.equals("Mover") && password.equals("ingod.asia");
								}
							};
							valid = context.status();
						}
					}
				}
			}
		}

		// If the user was not validated, fail with a
		// 401 status code (UNAUTHORIZED) and
		// pass back a WWW-Authenticate header for
		// this servlet.
		//
		// Note that this is the normal situation the
		// first time you access the page. The client
		// web browser will prompt for userID and password
		// and cache them so that it doesn't have to
		// prompt you again.

		if (!valid) {
			response.setHeader("WWW-Authenticate",
					"Basic realm=\"Login for Bible API\"");
			response.setStatus(401);

			return "Forbidden! Authentication failed!";
		}
		
		return this.feed();
	}

}