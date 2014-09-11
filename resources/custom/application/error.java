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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.application.Template;
import org.tinystruct.application.Variables;
import org.tinystruct.datatype.DataType;
import org.tinystruct.datatype.Variable;
import org.tinystruct.dom.Element;
import org.tinystruct.handle.Reforward;
import org.tinystruct.system.util.TextFileLoader;

import custom.objects.User;

public class error extends AbstractApplication {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private Reforward reforward;
	private User usr;

	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.setAction("error", "process");
		this.setAction("404", "not_found");
		
		this.setText("page.404.title");
		this.setText("navigator.404.caption");
	}

	@Override
	public String version() {
		// TODO Auto-generated method stub
		return null;
	}

	public String not_found() throws ApplicationException {
		final error app = this;
		this.request = (HttpServletRequest) this.context.getAttribute("HTTP_REQUEST");
		this.setVariable("action", this.config.get("default.base_url")+this.context.getAttribute("HTTP_REQUEST_ACTION").toString());
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

		this.setTemplate(new Template() {

			private String template_path;

			ConcurrentHashMap<String, Variable<?>> variables = Variables
					.getInstance();

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "404";
			}

			@Override
			public void setVariable(Variable<?> variable) {
				app.setVariable(variable);
			}

			@Override
			public Variable<?> getVariable(String name) {
				// TODO Auto-generated method stub
				return app.getVariable(name);
			}

			@Override
			public Map<String, Variable<?>> getVariables() {
				// TODO Auto-generated method stub
				return variables;
			}

			@Override
			public String parse() throws ApplicationException {
				// TODO Auto-generated method stub
				InputStream in;

				this.template_path = "themes" + File.separatorChar
						+ this.getName() + ".view";

				in = AbstractApplication.class.getClassLoader()
						.getResourceAsStream(this.template_path);

				TextFileLoader loader = new TextFileLoader(in);
				loader.setCharset(config.get("charset").toString());

				String tpl, value;

				tpl = loader.getContent().toString();

				Set<Entry<String, Variable<?>>> sets = variables.entrySet();
				Iterator<Entry<String, Variable<?>>> iterator = sets.iterator();

				List<Variable<?>> list = new ArrayList<Variable<?>>();
				Variable<?> variable;
				while (iterator.hasNext()) {
					Entry<String, Variable<?>> v = iterator.next();
					variable = v.getValue();

					if (variable.getType() == DataType.Array) {
						list.add(variable);
					} else {
						if (v.getKey().startsWith("[%LINK:")) {
							String base_url;
							if (context != null
									&& context.getAttribute("HTTP_HOST") != null)
								base_url = context.getAttribute("HTTP_HOST")
										.toString();
							else
								base_url = config.get("default.base_url")
										.toString();

							value = base_url + variable.getValue();
						} else
							value = variable.getValue().toString();

						tpl = tpl.replace(v.getKey(), value);
					}

				}

				return tpl;
			}
		});
		
		return this.getOutputText();
	}

	public void process() throws ApplicationException {
		this.request = (HttpServletRequest) this.context
				.getAttribute("HTTP_REQUEST");
		this.response = (HttpServletResponse) this.context
				.getAttribute("HTTP_RESPONSE");

		this.reforward = new Reforward(this.request, this.response);

		this.setVariable("from", this.reforward.getFromURL());

		HttpSession session = this.request.getSession();

		if (session.getAttribute("error") != null) {
			ApplicationException exception = (ApplicationException) session
					.getAttribute("error");

			String message = exception.getRootCause().getMessage();
			if (message != null)
				this.setVariable("exception.message", message);
			else
				this.setVariable("exception.message", "Unknown error");

			this.setVariable("exception.details", this.getDetail(exception)
					.toString());
		} else {
			this.reforward.forward();
		}
	}

	private Element getDetail(ApplicationException exception) {
		Element errors = new Element("ul");
		int i = 0;

		Throwable ex = exception.getRootCause();

		StackTraceElement[] trace = ex.getStackTrace();

		while (i < trace.length) {
			Element element = new Element("li");
			element.setData(trace[i++].toString());
			errors.addElement(element);
		}

		return errors;
	}

	public StringBuffer info() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("Protocol: " + this.request.getProtocol() + "\r\n");
		buffer.append("Scheme: " + this.request.getScheme() + "\r\n");
		buffer.append("Server Name: " + this.request.getServerName() + "\r\n");
		buffer.append("Server Port: " + this.request.getServerPort() + "\r\n");
		buffer.append("Protocol: " + this.request.getProtocol() + "\r\n");
		// buffer.append("Server Info: " +
		// getServletConfig().getServletContext().getServerInfo()+"\r\n");
		buffer.append("Remote Addr: " + this.request.getRemoteAddr() + "\r\n");
		buffer.append("Remote Host: " + this.request.getRemoteHost() + "\r\n");
		buffer.append("Character Encoding: "
				+ this.request.getCharacterEncoding() + "\r\n");
		buffer.append("Content Length: " + this.request.getContentLength()
				+ "\r\n");
		buffer.append("Content Type: " + this.request.getContentType() + "\r\n");
		buffer.append("Auth Type: " + this.request.getAuthType() + "\r\n");
		buffer.append("HTTP Method: " + this.request.getMethod() + "\r\n");
		buffer.append("Path Info: " + this.request.getPathInfo() + "\r\n");
		buffer.append("Path Trans: " + this.request.getPathTranslated()
				+ "\r\n");
		buffer.append("Query String: " + this.request.getQueryString() + "\r\n");
		buffer.append("Remote User: " + this.request.getRemoteUser() + "\r\n");
		buffer.append("Session Id: " + this.request.getRequestedSessionId()
				+ "\r\n");
		buffer.append("Request URI: " + this.request.getRequestURI() + "\r\n");
		buffer.append("Servlet Path: " + this.request.getServletPath() + "\r\n");
		buffer.append("Accept: " + this.request.getHeader("Accept") + "\r\n");
		buffer.append("Host: " + this.request.getHeader("Host") + "\r\n");
		buffer.append("Referer : " + this.request.getHeader("Referer") + "\r\n");
		buffer.append("Accept-Language : "
				+ this.request.getHeader("Accept-Language") + "\r\n");
		buffer.append("Accept-Encoding : "
				+ this.request.getHeader("Accept-Encoding") + "\r\n");
		buffer.append("User-Agent : " + this.request.getHeader("User-Agent")
				+ "\r\n");
		buffer.append("Connection : " + this.request.getHeader("Connection")
				+ "\r\n");
		buffer.append("Cookie : " + this.request.getHeader("Cookie") + "\r\n");

		return buffer;
	}

}
