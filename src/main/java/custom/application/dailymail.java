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

import custom.objects.*;
import custom.util.TaskDescriptor;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Element;
import org.tinystruct.mail.SimpleMail;
import org.tinystruct.system.scheduling.Scheduler;
import org.tinystruct.system.scheduling.SchedulerTask;
import org.tinystruct.system.scheduling.TimeIterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.SimpleDateFormat;
import java.util.*;

public class dailymail extends AbstractApplication implements ServletContextListener {

	private final Scheduler scheduler;
//	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
	private suggestion suggestion;
	private String locale;
	private dailymail mail;

	public dailymail() throws ApplicationException {
		// TODO Auto-generated constructor stub
		this.scheduler = new Scheduler(true);
		this.locale = this.getLocale().toString();
		
		mail = this;
	}

	public boolean start() {
		TimeIterator iterator = new TimeIterator(00, 00, 00);
		iterator.setInterval(3600 * 24);
		this.config.set("default.base_url", "http://www.ingod.today/?q=");
		this.scheduler.schedule(new SchedulerTask() {

			private Object o = new Object();
			private boolean next = false;

			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				scheduler.cancel();
			}

			@Override
			public void start() {
				// TODO Auto-generated method stub

				synchronized (o) {
//					System.out.println("\r\nstart..." + dateFormat.format(new Date()));

					try {

						SimpleMail themail = new SimpleMail();
						themail.setFrom("国际圣经在线");

						SimpleDateFormat format = new SimpleDateFormat(
								"MM-dd");
						plan plan = new plan();
						
						Date date = new Date();
						plan.findOneByKey("date", "2010-"+format.format(date)
								.toString());

						if (plan.getTask() == null) {
							throw new ApplicationException("Task is not ready!");
						}

						// start

						TaskDescriptor task = new TaskDescriptor();

						bible bible = new bible();
						
						if(locale.toString().equalsIgnoreCase(Locale.US.toString())) 
							bible.setTableName("NIV");
						else
							bible.setTableName("zh_CN");

						StringBuffer where = task.parse(plan.getTask());

						Table list = bible.findWith("WHERE " + where, new Object[] {});

						Iterator<Row> iterators = list.iterator();

						ArrayList<List<bible>> res = new ArrayList<List<bible>>();

						List<bible> bibles = new ArrayList<bible>();
						int bookId = 0, chapterId = 0;

						while (iterators.hasNext()) {
							
							bible bib = new bible();
							bib.setData(iterators.next());

							if (bookId != bib.getBookId()) {
								bookId = bib.getBookId();

								if (bibles.size() > 0)
									res.add(bibles);
								
								bibles = new ArrayList<bible>();
							}

							bibles.add(bib);
						}
						
						if (bibles.size() > 0)
							res.add(bibles);
						
						StringBuffer buffer=new StringBuffer();
						buffer.append("<div style=\"background: none repeat scroll 0 0 -moz-field; border: 1px solid threedshadow; margin: 2em auto; padding: 2em;\">");
						buffer.append("	<div>");
						buffer.append("		<a style=\"-moz-margin-end: 0; -moz-margin-start: 0.6em; float: right; margin-bottom: 0; margin-top: 0;\">");
						buffer.append("			<img id=\"feedTitleImage\" src=\"http://www.ingod.today/themes/images/favicon-b.png\"/> </a>");
						buffer.append("		<div style=\"-moz-margin-end: 0.6em; -moz-margin-start: 0; margin-bottom: 0; margin-top: 0;\">");
						buffer.append("			<h1 style=\"border-bottom: 2px solid threedlightshadow; font-size: 160%; margin: 0 0 0.2em;\">国际圣经在线</h1>");
//						buffer.append("			<h2 style=\"color: #C0C0C0; font-weight: normal; margin: 0 0 0.6em;\">国际圣经在线为立志跟随主耶稣基督的朋友提供圣经阅读、圣经收听、圣经检索、福音电影分享以及资源下载等服务</h2>");
						buffer.append("		</div>");
						buffer.append("	</div>");
						buffer.append("	<div id=\"feedContent\">");
						buffer.append("		<div class=\"entry\">");
						buffer.append("			<h2>");
						
						String date_string = new SimpleDateFormat("MM/dd").format(date);

						buffer.append("				<a href=\"http://www.ingod.today/?lang=zh-CN&amp;q=feed/"+date_string+"\">每日读经 "+date_string+"</a>");
						buffer.append("			</h2>");
						buffer.append("			<div base=\"http://www.ingod.today/?lang=zh-CN&amp;q=feed\" class=\"feedEntryContent\">");

						Iterator<List<bible>> iterator = res.iterator();
						
						book book = new book();
						while (iterator.hasNext()) {
							List<bible> bs = iterator.next();
							
							Table table = book.findWith("WHERE book_id=? and language=?",
									new Object[] { bs.get(0).getBookId(), locale.toString().equalsIgnoreCase(Locale.US.toString())?Locale.US.toString():"zh_CN" });
							if (table.size() > 0) {
								Row row = table.get(0);
								book.setData(row);
							}
							
							buffer.append("<h3><a href=\""+mail.getLink("bible")+"/"+book.getBookId()+"\">"+book.getBookName()+"</a></h3>");
							
							Iterator<bible> iter = bs.iterator();
							chapterId = 0;
							while (iter.hasNext()) {

								bible bi = iter.next();
								
								if(chapterId!=bi.getChapterId()) 
								{
									buffer.append("<h4><a href=\""+mail.getLink("bible")+"/"+book.getBookId()+"/"+bi.getChapterId()+"\">"+bi.getChapterId()+"章</a></h4>");
									
									chapterId = bi.getChapterId();
								}
								
								buffer.append(" ").append(bi.getPartId()).append(" ").append(bi.getContent());
							}
							
						}
						Element element = new Element();
						buffer.append(
										"<br /><a href=\""+mail.getLink("bible")+"\" style=\"float:right\">"
										+ mail.getProperty("subscribe.continue.caption")
										+ "</a>");
						
						buffer.append("			</div>");
						buffer.append("		</div>");
						buffer.append("		<div style=\"clear: both;\"></div>");
						buffer.append("	</div>");
						buffer.append("</div>");
						
						Element container = (Element) element.clone();
						container.setName("div");
						container.setAttribute("style", "background-color:#f5f5f5;text-align:justify");
						container.setData(buffer.toString());

						themail.setSubject("每日读经["
								+ format.format(new Date()).toString() + "]");

						Element footer = new Element("div");

						subscription subscription = new subscription();
						Table table = subscription.findWith("WHERE available = 1", new Object[]{});
						Iterator<Row> iterator1 = table.iterator();
						while (iterator1.hasNext()) {
							subscription.setData(iterator1.next());
							
							footer.setAttribute("style",
											"padding:10px;font-size:12px;color:#ccc;");
							footer.setData("让我们一起来养成每天读经的好习惯...<br />如果您不能正常访问此站点(<a href=\"http://www.ingod.today\">http://www.ingod.today</a>)，请尝试通过VPN或运行代理程序(Freegate7.01)后，再进行访问，给您带来不便请谅解！如果你不想收到此邮件，请点击<a href=\"http://www.ingod.today/?q=services/unsubscribe/"+subscription.getId()+"\">退订</a>。");

							themail.setBody(container.toString() + footer.toString());
							themail.setTo(subscription.getEmail());
							
							try {
								themail.send();
							}
							catch(ApplicationException e){
								subscription.setAvailable(false);
								subscription.update();
							}
						}

						// themail.attachFile("E:\\常用应用软件\\网络代理\\freegate7.01.rar");

						Thread.sleep(1);
					} catch (ApplicationException e) {
						suggestion = new suggestion();
						suggestion.setEmail("services@ingod.asia");
						suggestion.setIP("-");
						suggestion.setPostDate(new Date());
						suggestion.setStatus(false);
						suggestion.setTitle(e.getMessage() != null ? e
								.getMessage() : "");

						note(e,suggestion);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						suggestion = new suggestion();
						suggestion.setEmail("services@ingod.asia");
						suggestion.setIP("-");
						suggestion.setPostDate(new Date());
						suggestion.setStatus(false);
						suggestion.setTitle(e.getMessage() != null ? e
								.getMessage() : "");

						note(e,suggestion);
					} finally {
						this.next = true;
					}

//					System.out.println("\r\nstarted");
//					System.out.println("\r\nend..."	+ dateFormat.format(new Date()));
				}
			}

			@Override
			public boolean next() {
				// TODO Auto-generated method stub
				synchronized (o) {
					return this.next;
				}
			}

		}, iterator);

		return true;
	}

	public static void main(String[] args) throws ApplicationException {
		dailymail mailing = new dailymail();
		mailing.start();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		suggestion = new suggestion();
		suggestion.setEmail("services@ingod.asia");
		suggestion.setIP("-");
		suggestion.setPostDate(new Date());
		suggestion.setStatus(false);
		suggestion.setTitle("Task has been cancelled!");
		suggestion.setContent("Task has been cancelled at " + new Date());
		try {
			suggestion.append();
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.scheduler.cancel();
	}

	public void contextInitialized(ServletContextEvent sce) {
		this.start();
		
		suggestion = new suggestion();
		suggestion.setEmail("services@ingod.asia");
		suggestion.setIP("-");
		suggestion.setPostDate(new Date());
		suggestion.setStatus(false);
		suggestion.setTitle("Task has been started!");
		suggestion.setContent("Task has been started at " + new Date());
		try {
			suggestion.append();
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void note(Throwable e,suggestion suggestion) {
		// TODO Auto-generated method stub
		
		StackTraceElement[] trace = e.getStackTrace();

		StringBuffer errors = new StringBuffer("Details:\r\n");
		int i = 0;
		while (i < trace.length) {
			errors.append(trace[i++].toString() + "\r\n");
		}

		if (e.getCause() != null) {
			StackTraceElement[] cause = e.getCause().getStackTrace();

			errors.append("\r\nCaused by:" + e.getCause() + "\r\n");
			i = 0;
			while (i < cause.length) {
				errors.append(cause[i++].toString() + "\r\n");
			}
		}
		
		String content=errors.toString();
		suggestion.setContent(content);
		try {
			suggestion.append();
		} catch (ApplicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void init() {
		// TODO Auto-generated method stub
		this.setAction("start", "start");
	}

	public String version() {
		// TODO Auto-generated method stub
		return null;
	}
}
