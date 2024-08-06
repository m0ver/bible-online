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
import org.apache.http.ParseException;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Builder;
import org.tinystruct.handler.Reforward;
import org.tinystruct.http.*;
import org.tinystruct.http.client.HttpRequestBuilder;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.template.variable.ObjectVariable;
import org.tinystruct.system.util.StringUtilities;
import org.tinystruct.system.util.TextFileLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

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
    private static Builder clients;
    private passport passport;
    private User usr;

    @Action("user/login")
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
                    + "\"/>  <a href=\"javascript:void(0)\" onclick=\"restoreField()\">[%login.user.change%}</a>";

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
        this.setVariable("action", host.substring(0, host.lastIndexOf("/")) + "/?q=" + this.context.getAttribute("REQUEST_PATH").toString());

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

    @Action("user/logout")
    public Response logout() {
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
                    response.addHeader(Header.SET_COOKIE.name(), cookie.toString());
                    i++;
                }
            }

            Reforward reforward = new Reforward(request, response);
            reforward.setDefault(this.getLink(this.getConfiguration("default.login.page")));
            return reforward.forward();
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public void init() {
        TextFileLoader loader = new TextFileLoader();
        loader.setInputStream(login.class
                .getResourceAsStream("/clients_secrets.json"));
        clients = new Builder();
        try {
            clients.parse(loader.getContent().toString());
        } catch (ApplicationException e) {
            e.printStackTrace();
        }
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

        this.setVariable("error", "");
        this.setVariable("service", "");
        this.setVariable("application.summary", "");
    }

    @Action("validator/code")
    public String toImage() {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);

        ValidateCode code = new ValidateCode(request);
        return code.getEstablishedCode();
    }

    protected String createRequestString(oAuth2Provider provider)
            throws UnsupportedEncodingException {
        StringBuilder _builder = new StringBuilder();

        switch (provider) {
            case GITHUB:
                Builder builder = (Builder) clients.get("github");
                _builder
                        .append("https://github.com/login/oauth/authorize?")
                        .append("client_id=")
                        .append(builder.get("client_id"))
                        .append("&response_type=code")
                        .append("&scope=user:email")
                        .append("&response_type=code")
                        .append("&redirect_uri=")
                        .append(URLEncoder.encode(
                                this.getLink("oauth2_github_callback"), "utf8"));
                break;

            default:
                builder = (Builder) clients.get("web");

                _builder.append("https://accounts.google.com/o/oauth2/auth?");
                _builder.append("scope=");
                _builder.append(URLEncoder.encode(
                        String.join(" ", SCOPES), "utf-8"));
                _builder.append("&state=profile");
                _builder.append("&redirect_uri=");
                _builder.append(URLEncoder.encode(
                        this.getLink("oauth2callback"), "utf8"));
                _builder.append("&response_type=code");
                _builder.append("&client_id=").append(builder.get("client_id"));
                break;
        }

        return _builder.toString();
    }

    @Action("oauth2callback")
    public Response oAuth2callback() throws ApplicationException {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);
        Reforward reforward = new Reforward(request, response);
        TokenResponse oauth2_response;

        try {
            GoogleClientSecrets clientSecrets;
            if (this.getVariable("google_client_secrets") == null) {
                clientSecrets = GoogleClientSecrets.load(
                        JSON_FACTORY,
                        new InputStreamReader(Objects.requireNonNull(login.class
                                .getResourceAsStream("/clients_secrets.json"))));
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
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e1.getMessage(), e1);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e.getMessage(), e);
        }

        try {
//            String url = "https://www.google.com/m8/feeds/contacts/default/full";
            HttpRequestBuilder builder = new HttpRequestBuilder();
            builder.setParameter("access_token", oauth2_response.getAccessToken());

            URLRequest urlRequest = new URLRequest(new URL("https://www.googleapis.com/oauth2/v1/userinfo"));
            byte[] bytes = urlRequest.send(builder);

            Builder userinfo = new Builder();
            userinfo.parse(new String(bytes));
            System.out.println("Ok:" + userinfo);

            if (userinfo.get("email") != null) {
                this.usr = new User();
                this.usr.setEmail(userinfo.get("email").toString());
                if (this.usr.findOneByKey("email", this.usr.getEmail()).isEmpty()) {
                    usr.setPassword("");
                    usr.setUsername(usr.getEmail());
                    usr.setLastloginTime(new Date());
                    usr.setRegistrationTime(new Date());
                    usr.append();
                }

                new passport(request, response, "waslogined")
                        .setLoginAsUser(this.usr.getId());

                reforward.setDefault(URLDecoder.decode(this.getVariable("from")
                        .getValue().toString(), StandardCharsets.UTF_8));
                return reforward.forward();
            }
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

        return response;
    }

    @Action("oauth2_github_callback")
    public Response oAuth2GithubCallback() throws ApplicationException {
        Request request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);
        Reforward reforward = new Reforward(request, response);

        Builder client = null;
        if (this.getVariable("github_client_secrets") == null) {
            if (clients.get("github") instanceof Builder) {
                client = (Builder) clients.get("github");
                this.setVariable(new ObjectVariable("github_client_secrets",
                        client), false);
            }
        } else {
            client = (Builder) this.getVariable("github_client_secrets")
                    .getValue();
        }

        if (client != null) {
            try {
                URLRequest urlRequest = new URLRequest(new URL("https://github.com/login/oauth/access_token?client_id="
                        + client.get("client_id")
                        + "&client_secret="
                        + client.get("client_secret")
                        + "&code="
                        + request.getParameter("code")));
                byte[] resp = urlRequest.send(new HttpRequestBuilder());
                String tokens = new String(resp);
                StringTokenizer tokenizer = new StringTokenizer(tokens, "&");
                HashMap<String, String> map = new HashMap<>();
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    int index = token.indexOf("=");
                    String key = token.substring(0, index);
                    String value = token.substring(index + 1);
                    map.put(key, value);
                }

                if (!map.isEmpty()) {
                    HttpRequestBuilder builder = new HttpRequestBuilder();
                    if (map.get("access_token") != null) {
                        Headers headers = new Headers();
                        headers.add(Header.AUTHORIZATION.set("Bearer " + map.get("access_token")));
                        builder.setHeaders(headers);
                    }
                    if (map.get("scope") != null) {
                        builder.setParameter("scope", map.get("scope"));
                    }

                    urlRequest.setUrl(new URL("https://api.github.com/user"));
                    byte[] bytes = urlRequest.send(builder);

                    Builder struct = new Builder();
                    struct.parse(new String(bytes));

                    if (struct.get("email") != null) {
                        this.usr = new User();
                        this.usr.setEmail(struct.get("email").toString());

                        if (this.usr.findOneByKey("email", this.usr.getEmail()).size() == 0) {
                            usr.setPassword("");
                            usr.setUsername(usr.getEmail());
                            usr.setLastloginTime(new Date());
                            usr.setRegistrationTime(new Date());
                            usr.append();
                        }

                        new passport(request, response, "waslogined")
                                .setLoginAsUser(this.usr.getId());

                        reforward.setDefault(URLDecoder.decode(this.getVariable("from")
                                .getValue().toString(), StandardCharsets.UTF_8));

                        return reforward.forward();
                    }
                } else {
                    throw new ApplicationException("Invalid Response.");
                }
            } catch (IOException e) {
                throw new ApplicationException(e.getMessage(), e);
            } catch (ParseException e) {
                throw new ApplicationException(e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new ApplicationException(e.getMessage(), e);
            }
        }

        return response;
    }

    @Action("user/account")
    public Response execute(String provider) throws ApplicationException {
        Request http_request = (Request) this.context
                .getAttribute(HTTP_REQUEST);
        Response http_response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);

        Reforward reforward = new Reforward(http_request, http_response);
        this.setVariable("from", reforward.getFromURL());
        try {
            Session session = http_request.getSession();
            if (session.getAttribute("usr") == null)
                reforward.setDefault(createRequestString(oAuth2Provider
                        .valueOf(provider.toUpperCase())));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e.getMessage(), e);
        }
        return reforward.forward();
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }
}