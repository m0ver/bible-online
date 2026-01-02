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
import org.tinystruct.application.ActionRegistry;
import org.tinystruct.application.Variables;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.ResponseHeaders;
import org.tinystruct.http.Session;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.template.DefaultTemplate;

import java.io.InputStream;
import java.util.*;

import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class help extends AbstractApplication {

    @Override
    public void init() {
        this.setTemplateRequired(false);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.condition.title", locale);
        this.setText("application.title", locale);
        this.setText("application.language.name", locale);

        this.setText("page.welcome.caption", locale);
        this.setText("page.language-setting.title", locale);
        this.setText("page.logout.caption", locale);

        this.setText("navigator.bible.caption", locale);
        this.setText("navigator.video.caption", locale);
        this.setText("navigator.document.caption", locale);
        this.setText("navigator.reader.caption", locale);
        this.setText("navigator.controller.caption", locale);
        this.setText("navigator.help.caption", locale);

        this.setText("footer.report-a-site-bug", locale);
        this.setText("footer.privacy", locale);
        this.setText("footer.register", locale);
        this.setText("footer.api", locale);
        this.setText("footer.updates-rss", locale);

        this.setVariable("TEMPLATES_DIR", "/themes");

        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setText("page.welcome.hello", (username == null || username.trim().isEmpty()) ? "" : username + "，");
    }

    @Action("help")
    public String privacy(Request request) throws ApplicationException {
        String host = String.valueOf(getContext().getAttribute("HTTP_HOST"));
        // remove the default language for action
        this.setVariable("action", host.substring(0, host.lastIndexOf("/")) + "/?q=" + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile",
                    "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"
                            + usr.getEmail() + "</a>");
        } else {
            this.setVariable(
                    "user.status",
                    "<a href=\"" + this.getLink("user/login") + "\">"
                            + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        InputStream in = help.class.getClassLoader().getResourceAsStream("themes/privacy.view");
        return this.setTemplate(new DefaultTemplate(this, in, Variables.getInstance(getLocale().toString()).getVariables()));
    }

    @Action("help/condition")
    public String condition(Request request) throws ApplicationException {
        String host = String.valueOf(getContext().getAttribute("HTTP_HOST"));
        // remove the default language for action
        this.setVariable("action", host.substring(0, host.lastIndexOf("/")) + "/?q=" + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile",
                    "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"
                            + usr.getEmail() + "</a>");
        } else {
            this.setVariable(
                    "user.status",
                    "<a href=\"" + this.getLink("user/login") + "\">"
                            + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        InputStream in = help.class.getClassLoader().getResourceAsStream("themes/condition.view");
        return this.setTemplate(new DefaultTemplate(this, in, Variables.getInstance(getLocale().toString()).getVariables()));
    }

    @Action("sitemap.xml")
    public Object sitemap() {
        Collection<String> list = ActionRegistry.getInstance().paths();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\r\n");
        buffer.append("<url>\r\n");
        buffer.append("  <loc>https://www.ingod.today/</loc>\r\n");
        buffer.append("  <changefreq>daily</changefreq>\r\n");
        buffer.append("  <priority>1.00</priority>\r\n");
        buffer.append("</url>\r\n");

        buffer.append("<url>\r\n");
        buffer.append("  <loc>https://www.ingod.today/bible.pdf</loc>\r\n");
        buffer.append("  <changefreq>daily</changefreq>\r\n");
        buffer.append("  <priority>0.90</priority>\r\n");
        buffer.append("</url>\r\n");

        String path;
        for (String s : list) {
            path = s;
            if (path.equals("#") || this.getLink(path).equals("#")) continue;
            buffer.append("<url>\r\n");
            buffer.append("  <loc>").append(this.getLink(path).replace("&", "&amp;")).append("</loc>\r\n");
            buffer.append("  <changefreq>weekly</changefreq>\r\n");
            buffer.append("  <priority>0.80</priority>\r\n");
            buffer.append("</url>\r\n");
        }

        buffer.append("</urlset>");

        Response response = (Response) getContext()
                .getAttribute(HTTP_RESPONSE);

        ResponseHeaders headers = new ResponseHeaders(response);
        headers.add(org.tinystruct.http.Header.CONTENT_TYPE.set("text/xml;charset="
                + getConfiguration().getOrDefault("charset", "UTF-8")));

        return buffer;
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}
