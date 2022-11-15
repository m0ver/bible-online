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

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import custom.objects.User;
import custom.util.ValidateCode;
import custom.util.model.oAuth2Provider;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Builder;
import org.tinystruct.data.component.Struct;
import org.tinystruct.handler.Reforward;
import org.tinystruct.http.*;
import org.tinystruct.system.template.variable.ObjectVariable;
import org.tinystruct.system.util.StringUtilities;
import org.tinystruct.system.util.TextFileLoader;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class login extends AbstractApplication {
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    /**
     * OAuth 2.0 scopes.
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.google.com/m8/feeds");
    private static GoogleClientSecrets clientSecrets;
    private static Builder builder;
    private passport passport;
    private User usr;

    public static void main(String[] args) throws ApplicationException, NoSuchAlgorithmException {
//		TextFileLoader loader = new TextFileLoader();
//		loader.setInputStream(login.class
//				.getResourceAsStream("/clients_secrets.json"));
//		Builder builder = new Builder();
//		builder.parse(loader.getContent().toString());
//
//		if (builder.get("github") instanceof Builder)
//			builder = (Builder) builder.get("github");
//
//		System.out.println(builder.get("client_secret"));
//		System.out.println(builder.get("client_id"));


    }

    public Object validate() {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);

        Cookie cookie = StringUtilities.getCookieByName(request.cookies(),
                "username");
        if (cookie != null) {
            this.setVariable("username", cookie.value());
            String user_field = cookie.value()
                    + "<input class=\"text\" id=\"username\" name=\"username\" type=\"hidden\" value=\""
                    + cookie.value()
                    + "\"/>  <a href=\"javascript:void(0)\" onclick=\"restoreField()\">[%login.user.change%]</a>";

            this.setVariable("user_field", user_field);
        } else {
            this.setVariable("username", "");
            this.setVariable(
                    "user_field",
                    "<input class=\"text\" id=\"username\" name=\"username\" type=\"text\" value=\"\"/>");
        }

        this.setText("login.tips.text", this.getLink("bible"));

        try {
            Reforward reforward = new Reforward(request, response);

            if (request.method() == Method.POST) {
                this.passport = new passport(request, response, "waslogined");
                if (this.passport.login()) {
                    return reforward.forward();
                }
            }

            this.setVariable("from", reforward.getFromURL());
        } catch (ApplicationException e) {
            this.setVariable("error", "<div class=\"error\">"
                    + e.getRootCause().getMessage() + "</div>");
        }

        String host = String.valueOf(this.context.getAttribute("HTTP_HOST"));
        // remove the default language for action
        this.setVariable("action", host.substring(0, host.lastIndexOf("/")) + "/?q=" + this.context.getAttribute("REQUEST_ACTION").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            this.usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile",
                    "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"
                            + this.usr.getEmail() + "</a>");
        } else {
            this.setVariable(
                    "user.status",
                    "<a href=\"" + this.getLink("user/login") + "\">"
                            + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        this.setVariable("code", this.toImage());

        return this;
    }

    public void logout() {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);

        try {
            this.passport = new passport(request, response, "waslogined");
            this.passport.logout();

            if (request.cookies() != null) {
                Cookie[] cookies = request.cookies();
                int i = 0;
                Cookie cookie;
                while (cookies.length > i) {
                    cookie = cookies[i];
                    cookie.setMaxAge(0);
                    cookie.setValue("");
                    response.addHeader(org.tinystruct.http.Header.SET_COOKIE.toString(), cookie.toString());
                    i++;
                }
            }

            Reforward reforward = new Reforward(request, response);
            reforward.setDefault(this.getLink(this.getConfiguration("default.login.page")));
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
//        this.setAction("validator/code", "toImage");
        this.setAction("user/account", "execute");
        this.setAction("oauth2callback", "oAuth2callback");
        this.setAction("oauth2_github_callback", "oAuth2_github_callback");

        this.setSharedVariable("error", "");
        this.setSharedVariable("service", "");
        this.setSharedVariable("application.summary", "");
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

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
        this.setText("login.with.otheraccount");

        this.setText("navigator.login.caption");
        this.setText("footer.forgot");
        this.setText("page.login.title");
        this.setText("page.welcome.caption");
        this.setText("page.language-setting.title");
        this.setText("page.logout.caption");

        this.setText("application.title");
        this.setText("application.language.name");

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

        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setText("page.welcome.hello", (username == null || username.trim()
                .length() == 0) ? "" : username + "，");
    }

    public String toImage() {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);

        ValidateCode code = new ValidateCode(request);
        return code.getEstablishedCode();
    }

    protected String createRequestString(oAuth2Provider provider)
            throws UnsupportedEncodingException {
        StringBuffer requestBuffer = new StringBuffer();

        switch (provider) {
            case GITHUB:
                requestBuffer
                        .append("https://github.com/login/oauth/authorize?")
                        .append("client_id=9db0327fa27efc4449af")
                        .append("&response_type=code")
                        .append("&scope=user:email")
                        .append("&redirect_uri=")
                        .append(URLEncoder.encode(
                                this.getLink("oauth2_github_callback"), "utf8"));
                break;

            default:
                requestBuffer.append("https://accounts.google.com/o/oauth2/auth?");
                requestBuffer.append("scope=");
                requestBuffer.append(URLEncoder.encode(
                        StringUtilities.implode(" ", SCOPES), "utf-8"));
                requestBuffer.append("&state=profile");
                requestBuffer.append("&redirect_uri=");
                requestBuffer.append(URLEncoder.encode(
                        this.getLink("oauth2callback"), "utf8"));
                requestBuffer.append("&response_type=code");
                requestBuffer.append("&client_id=850344213005-lgjck8o3b3u75l8hpa8ihn8d56omvd06.apps.googleusercontent.com");
                break;

        }
        return requestBuffer.toString();
    }

    public String oAuth2callback() throws ApplicationException {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);
        Reforward reforward = new Reforward(request, response);
        TokenResponse oauth2_response;

        try {
            if (this.getVariable("google_client_secrets") == null) {
                clientSecrets = GoogleClientSecrets.load(
                        JSON_FACTORY,
                        new InputStreamReader(login.class
                                .getResourceAsStream("/clients_secrets.json")));
                if (clientSecrets.getDetails().getClientId()
                        .startsWith("Enter")
                        || clientSecrets.getDetails().getClientSecret()
                        .startsWith("Enter ")) {
                    System.out
                            .println("Enter Client ID and Secret from https://code.google.com/apis/console/ ");
                }

                this.setVariable(new ObjectVariable("google_client_secrets",
                        clientSecrets), false);
            } else
                clientSecrets = (GoogleClientSecrets) this.getVariable(
                        "google_client_secrets").getValue();

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                    clientSecrets, SCOPES).build();

            oauth2_response = flow
                    .newTokenRequest(request.getParameter("code"))
                    .setRedirectUri(this.getLink("oauth2callback")).execute();

            System.out.println("Ok:" + oauth2_response.toString());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e1.getMessage(), e1);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e.getMessage(), e);
        }

        try {
            HttpClient httpClient = new DefaultHttpClient();
            String url = "https://www.google.com/m8/feeds/contacts/default/full";
            url = "https://www.googleapis.com/oauth2/v1/userinfo";
            HttpGet httpget = new HttpGet(url + "?access_token="
                    + oauth2_response.getAccessToken());
            httpClient.getParams().setParameter(
                    HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");

            HttpResponse http_response = httpClient.execute(httpget);
            HeaderIterator iterator = http_response.headerIterator();
            while (iterator.hasNext()) {
                Header next = iterator.nextHeader();
                System.out.println(next.getName() + ":" + next.getValue());
            }
            com.google.api.client.http.HttpTransport h;

            InputStream instream = http_response.getEntity().getContent();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];

            int len;
            while ((len = instream.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            instream.close();
            out.close();

            Struct struct = new Builder();
            struct.parse(new String(out.toByteArray(), "utf-8"));

            this.usr = new User();
            this.usr.setEmail(struct.toData().getFieldInfo("email")
                    .stringValue());

            if (this.usr.findOneByKey("email", this.usr.getEmail()).size() == 0) {
                usr.setPassword("");
                usr.setUsername(usr.getEmail());

//                usr.setLastloginIP(request.getRemoteAddr());
                usr.setLastloginTime(new Date());
                usr.setRegistrationTime(new Date());
                usr.append();
            }

            new passport(request, response, "waslogined")
                    .setLoginAsUser(this.usr.getId());

            reforward.setDefault(URLDecoder.decode(this.getVariable("from")
                    .getValue().toString(), "utf8"));
            reforward.forward();

            return new String(out.toByteArray(), "utf-8");
        } catch (ClientProtocolException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

    }

    public String oAuth2_github_callback() throws ApplicationException {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);
        Reforward reforward = new Reforward(request, response);

        if (this.getVariable("github_client_secrets") == null) {
            TextFileLoader loader = new TextFileLoader();
            loader.setInputStream(login.class
                    .getResourceAsStream("/clients_secrets.json"));
            builder = new Builder();
            builder.parse(loader.getContent().toString());

            if (builder.get("github") instanceof Builder) {
                builder = (Builder) builder.get("github");

                System.out.println(builder.get("client_secret"));
                System.out.println(builder.get("client_id"));
                this.setVariable(new ObjectVariable("github_client_secrets",
                        builder), false);
            }
        } else
            builder = (Builder) this.getVariable("github_client_secrets")
                    .getValue();

        String arguments = this
                .http_client("https://github.com/login/oauth/access_token?client_id="
                        + builder.get("client_id")
                        + "&client_secret="
                        + builder.get("client_secret")
                        + "&code="
                        + request.getParameter("code"));

        try {

            HttpClient httpClient = new DefaultHttpClient();
            String url = "https://api.github.com/user";

            HttpGet httpget = new HttpGet(url + "?" + arguments);
            httpClient.getParams().setParameter(
                    HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");

            HttpResponse http_response = httpClient.execute(httpget);
            HeaderIterator iterator = http_response.headerIterator();
            while (iterator.hasNext()) {
                Header next = iterator.nextHeader();
                System.out.println(next.getName() + ":" + next.getValue());
            }

            InputStream instream = http_response.getEntity().getContent();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];

            int len;
            while ((len = instream.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            instream.close();
            out.close();

            Struct struct = new Builder();
            struct.parse(new String(out.toByteArray(), "utf-8"));

            this.usr = new User();
            this.usr.setEmail(struct.toData().getFieldInfo("email")
                    .stringValue());

            if (this.usr.findOneByKey("email", this.usr.getEmail()).size() == 0) {
                usr.setPassword("");
                usr.setUsername(usr.getEmail());

//                usr.setLastloginIP(request.getRemoteAddr());
                usr.setLastloginTime(new Date());
                usr.setRegistrationTime(new Date());
                usr.append();
            }

            new passport(request, response, "waslogined")
                    .setLoginAsUser(this.usr.getId());

            reforward.setDefault(URLDecoder.decode(this.getVariable("from")
                    .getValue().toString(), "utf8"));
            reforward.forward();

            return new String(out.toByteArray(), "utf-8");
        } catch (ClientProtocolException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public String http_client(String url) throws ApplicationException {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpget = new HttpGet(url);
        httpClient.getParams().setParameter(
                HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");

        HttpResponse http_response;
        try {
            http_response = httpClient.execute(httpget);

            HeaderIterator iterator = http_response.headerIterator();
            while (iterator.hasNext()) {
                Header next = iterator.nextHeader();
                System.out.println(next.getName() + ":" + next.getValue());
            }

            InputStream instream = http_response.getEntity().getContent();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];

            int len;
            while ((len = instream.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            instream.close();
            out.close();

            return new String(out.toByteArray(), "utf-8");
        } catch (ClientProtocolException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public void execute(String provider) throws ApplicationException {
        Request http_request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response http_response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);

        Reforward reforward = new Reforward(http_request, http_response);
        this.setVariable("from", reforward.getFromURL());
        System.out.println("From:" + reforward.getFromURL());
        try {
            Session session = http_request.getSession();
            if (session.getAttribute("usr") == null)
                reforward.setDefault(createRequestString(oAuth2Provider
                        .valueOf(provider.toUpperCase())));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e.getMessage(), e);
        }
        reforward.forward();
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }
}