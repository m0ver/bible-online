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

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.http.Response;
import org.tinystruct.http.ResponseHeaders;
import org.tinystruct.system.ApplicationManager;
import org.tinystruct.system.cli.CommandLine;
import org.tinystruct.system.template.DefaultTemplate;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class help extends AbstractApplication {

    @Override
    public void init() {
        // TODO Auto-generated method stub
        this.setAction("help", "privacy");
        this.setAction("help/condition", "condition");
        this.setAction("sitemap.xml", "sitemap");
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.condition.title");
        this.setText("application.title");
        this.setText("application.language.name");

        this.setText("page.welcome.caption");
        this.setText("page.language-setting.title");
        this.setText("page.logout.caption");

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
    }

    public void privacy() throws ApplicationException {
        InputStream in = help.class.getClassLoader().getResourceAsStream("themes/privacy.view");
        this.setTemplate(new DefaultTemplate(this, in));
    }

    public void condition() throws ApplicationException {
        InputStream in = help.class.getClassLoader().getResourceAsStream("themes/condition.view");
        this.setTemplate(new DefaultTemplate(this, in));
    }

    public Object sitemap() {
        scripture scripture = (scripture) ApplicationManager.get(scripture.class.getName());
        scripture.init();
        Map<String, CommandLine> commandLines = scripture.getCommandLines();
        Set<String> list = commandLines.keySet();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");
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
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            path = iterator.next();
            if (path.equals("#") || this.getLink(path).equals("#")) continue;
            buffer.append("<url>\r\n");
            buffer.append("  <loc>" + this.getLink(path).replace("&", "&amp;") + "</loc>\r\n");
            buffer.append("  <changefreq>weekly</changefreq>\r\n");
            buffer.append("  <priority>0.80</priority>\r\n");
            buffer.append("</url>\r\n");
        }

        buffer.append("</urlset>");

        Response response = (Response) this.context
                .getAttribute(HTTP_RESPONSE);

        ResponseHeaders headers = new ResponseHeaders(response);
        headers.add(org.tinystruct.http.Header.CONTENT_TYPE.set("text/xml;charset="
                + this.config.get("charset")));

        return buffer;
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}
