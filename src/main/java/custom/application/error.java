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
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.application.Template;
import org.tinystruct.application.Variables;
import org.tinystruct.dom.Element;
import org.tinystruct.handler.Reforward;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.ResponseStatus;
import org.tinystruct.http.Session;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.template.variable.DataType;
import org.tinystruct.system.template.variable.Variable;
import org.tinystruct.system.util.TextFileLoader;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class error extends AbstractApplication {

    /**
     * Status code (100) indicating the client can continue.
     */
    public static final int CONTINUE = 100;

    /**
     * Status code (101) indicating the server is switching protocols
     * according to Upgrade header.
     */
    public static final int SWITCHING_PROTOCOLS = 101;

    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int OK = 200;

    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */
    public static final int CREATED = 201;

    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */
    public static final int ACCEPTED = 202;

    /**
     * Status code (203) indicating that the meta information presented
     * by the client did not originate from the server.
     */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */
    public static final int NO_CONTENT = 204;

    /**
     * Status code (205) indicating that the agent <em>SHOULD</em> reset
     * the document view which caused the request to be sent.
     */
    public static final int RESET_CONTENT = 205;

    /**
     * Status code (206) indicating that the server has fulfilled
     * the partial GET request for the resource.
     */
    public static final int PARTIAL_CONTENT = 206;

    /**
     * Status code (300) indicating that the requested resource
     * corresponds to any one of a set of representations, each with
     * its own specific location.
     */
    public static final int MULTIPLE_CHOICES = 300;

    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */
    public static final int MOVED_PERMANENTLY = 301;

    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     * <p>
     * This definition is being retained for backwards compatibility.
     * FOUND is now the preferred definition.
     */
    public static final int MOVED_TEMPORARILY = 302;

    /**
     * Status code (302) indicating that the resource reside
     * temporarily under a different URI. Since the redirection might
     * be altered on occasion, the client should continue to use the
     * Request-URI for future requests.(HTTP/1.1) To represent the
     * status code (302), it is recommended to use this variable.
     */
    public static final int FOUND = 302;

    /**
     * Status code (303) indicating that the response to the request
     * can be found under a different URI.
     */
    public static final int SEE_OTHER = 303;

    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * Status code (305) indicating that the requested resource
     * <em>MUST</em> be accessed through the proxy given by the
     * <code><em>Location</em></code> field.
     */
    public static final int USE_PROXY = 305;

    /**
     * Status code (307) indicating that the requested resource
     * resides temporarily under a different URI. The temporary URI
     * <em>SHOULD</em> be given by the <code><em>Location</em></code>
     * field in the response.
     */
    public static final int TEMPORARY_REDIRECT = 307;

    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * Status code (402) reserved for future use.
     */
    public static final int PAYMENT_REQUIRED = 402;

    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
    public static final int FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    public static final int NOT_FOUND = 404;

    /**
     * Status code (405) indicating that the method specified in the
     * <code><em>Request-Line</em></code> is not allowed for the resource
     * identified by the <code><em>Request-URI</em></code>.
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * Status code (406) indicating that the resource identified by the
     * request is only capable of generating response entities which have
     * content characteristics not acceptable according to the accept
     * headers sent in the request.
     */
    public static final int NOT_ACCEPTABLE = 406;

    /**
     * Status code (407) indicating that the client <em>MUST</em> first
     * authenticate itself with the proxy.
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * Status code (408) indicating that the client did not produce a
     * request within the time that the server was prepared to wait.
     */
    public static final int REQUEST_TIMEOUT = 408;

    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */
    public static final int CONFLICT = 409;

    /**
     * Status code (410) indicating that the resource is no longer
     * available at the server and no forwarding address is known.
     * This condition <em>SHOULD</em> be considered permanent.
     */
    public static final int GONE = 410;

    /**
     * Status code (411) indicating that the request cannot be handled
     * without a defined <code><em>Content-Length</em></code>.
     */
    public static final int LENGTH_REQUIRED = 411;

    /**
     * Status code (412) indicating that the precondition given in one
     * or more of the request-header fields evaluated to false when it
     * was tested on the server.
     */
    public static final int PRECONDITION_FAILED = 412;

    /**
     * Status code (413) indicating that the server is refusing to process
     * the request because the request entity is larger than the server is
     * willing or able to process.
     */
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * Status code (414) indicating that the server is refusing to service
     * the request because the <code><em>Request-URI</em></code> is longer
     * than the server is willing to interpret.
     */
    public static final int REQUEST_URI_TOO_LONG = 414;

    /**
     * Status code (415) indicating that the server is refusing to service
     * the request because the entity of the request is in a format not
     * supported by the requested resource for the requested method.
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * Status code (416) indicating that the server cannot serve the
     * requested byte range.
     */
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * Status code (417) indicating that the server could not meet the
     * expectation given in the Expect request header.
     */
    public static final int EXPECTATION_FAILED = 417;

    /**
     * Status code (500) indicating an error inside the HTTP server
     * which prevented it from fulfilling the request.
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * Status code (501) indicating the HTTP server does not support
     * the functionality needed to fulfill the request.
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */
    public static final int BAD_GATEWAY = 502;

    /**
     * Status code (503) indicating that the HTTP server is
     * temporarily overloaded, and unable to handle the request.
     */
    public static final int SERVICE_UNAVAILABLE = 503;

    /**
     * Status code (504) indicating that the server did not receive
     * a timely response from the upstream server while acting as
     * a gateway or proxy.
     */
    public static final int GATEWAY_TIMEOUT = 504;

    /**
     * Status code (505) indicating that the server does not support
     * or refuses to support the HTTP protocol version that was used
     * in the request message.
     */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    private Request request;
    private Response response;
    private Reforward reforward;
    private User usr;

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.404.title");
        this.setText("navigator.404.caption");

        this.setVariable("TEMPLATES_DIR", "/themes");
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    @Action("404")
    public error not_found() throws ApplicationException {
        final error app = this;
        this.request = (Request) getContext().getAttribute(HTTP_REQUEST);
        this.response = (Response) getContext().getAttribute(HTTP_RESPONSE);
        this.response.setStatus(ResponseStatus.NOT_FOUND);

        this.setVariable("action", getConfiguration().get("default.base_url") + getContext().getAttribute("REQUEST_PATH").toString());
        this.setVariable("base_url", String.valueOf(getContext().getAttribute("HTTP_HOST")));

        Session session = this.request.getSession();
        if (session.getAttribute("usr") != null) {
            this.usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + this.usr.getEmail() + "</a>");
            this.setVariable("scripts", "$.ajax({url:\"" + this.getLink("services/getwords") + "\",dataType:\"xml\",type:'GET'}).success(function(data){data=wordsXML(data);ldialog.show(data);});");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
            this.setVariable("scripts", "");
        }

        this.setTemplate(new Template() {

            Map<String, Variable<?>> variables = Variables
                    .getInstance(app.getLocale().toString()).getVariables();
            private String template_path;

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return "404";
            }

            @Override
            public void setVariable(Variable<?> variable) {
                app.setVariable(variable, true);
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
                loader.setCharset(getConfiguration().get("charset").toString());

                String tpl, value;
                tpl = loader.getContent().toString();

                Set<Entry<String, Variable<?>>> sets = variables.entrySet();
                Iterator<Entry<String, Variable<?>>> iterator = sets.iterator();

                List<Variable<?>> list = new ArrayList<Variable<?>>();
                Variable<?> variable;
                while (iterator.hasNext()) {
                    Entry<String, Variable<?>> v = iterator.next();
                    variable = v.getValue();

                    if (variable.getType() == DataType.ARRAY) {
                        list.add(variable);
                    } else {
                        if (v.getKey().startsWith("[%LINK:")) {
                            String base_url;
                            if (getContext() != null
                                    && getContext().getAttribute("HTTP_HOST") != null)
                                base_url = getContext().getAttribute("HTTP_HOST")
                                        .toString();
                            else
                                base_url = getConfiguration().get("default.base_url");

                            value = base_url + variable.getValue();
                        } else
                            value = variable.getValue().toString();

                        tpl = tpl.replace(v.getKey(), value);
                    }

                }

                return tpl;
            }
        });

        return this;
    }

    @Action("process")
    public void process() throws ApplicationException {
        this.request = (Request) getContext().getAttribute(HTTP_REQUEST);
        this.response = (Response) getContext().getAttribute(HTTP_RESPONSE);
        this.response.setStatus(ResponseStatus.FORBIDDEN);
        this.reforward = new Reforward(this.request, this.response);

        this.setVariable("from", this.reforward.getFromURL());

        Session session = this.request.getSession();
        if (session.getAttribute("error") != null) {
            ApplicationException exception = (ApplicationException) session.getAttribute("error");

            String message = exception.getRootCause().getMessage();
            if (message != null)
                this.setVariable("exception.message", message);
            else
                this.setVariable("exception.message", "Unknown error");

            this.setVariable("exception.details", this.getDetail(exception)
                    .toString());

            this.response.setStatus(ResponseStatus.valueOf(exception.getStatus()));
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
/*
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
		buffer.append("Character Encoding: " + this.request.getCharacterEncoding() + "\r\n");
		buffer.append("Content Length: " + this.request.getContentLength() + "\r\n");
		buffer.append("Content Type: " + this.request.getContentType() + "\r\n");
		buffer.append("Auth Type: " + this.request.getAuthType() + "\r\n");
		buffer.append("HTTP Method: " + this.request.getMethod() + "\r\n");
		buffer.append("Path Info: " + this.request.getPathInfo() + "\r\n");
		buffer.append("Path Trans: " + this.request.getPathTranslated() + "\r\n");
		buffer.append("Query String: " + this.request.getQueryString() + "\r\n");
		buffer.append("Remote User: " + this.request.getRemoteUser() + "\r\n");
		buffer.append("Session Id: " + this.request.getRequestedSessionId() + "\r\n");
		buffer.append("Request URI: " + this.request.getRequestURI() + "\r\n");
		buffer.append("Servlet Path: " + this.request.getServletPath() + "\r\n");
		buffer.append("Accept: " + this.request.getHeader("Accept") + "\r\n");
		buffer.append("Host: " + this.request.getHeader("Host") + "\r\n");
		buffer.append("Referer : " + this.request.getHeader("Referer") + "\r\n");
		buffer.append("Accept-Language : " + this.request.getHeader("Accept-Language") + "\r\n");
		buffer.append("Accept-Encoding : " + this.request.getHeader("Accept-Encoding") + "\r\n");
		buffer.append("User-Agent : " + this.request.getHeader("User-Agent") + "\r\n");
		buffer.append("Connection : " + this.request.getHeader("Connection") + "\r\n");
		buffer.append("Cookie : " + this.request.getHeader("Cookie") + "\r\n");

		return buffer;
	}
*/
}
