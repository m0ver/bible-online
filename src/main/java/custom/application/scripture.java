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
import custom.objects.bible;
import custom.objects.book;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.application.SharedVariables;
import org.tinystruct.data.component.Cache;
import org.tinystruct.data.component.Field;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Element;
import org.tinystruct.http.*;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.security.Authentication;
import org.tinystruct.system.security.Credential;
import org.tinystruct.system.template.variable.DataVariable;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class scripture extends AbstractApplication {
    private final static Cache data = Cache.getInstance();

    @Override
    public void init() {
        book book = new book();

        try {
            Table list = book.findAll();

            Iterator<Row> item = list.iterator();
            String bookName;
            while (item.hasNext()) {
                book.setData(item.next());

                bookName = book.getBookName().trim();
                String[] lang = book.getLanguage().split("_");
                Locale locale = new Locale(lang[0], lang[1]);

                this.setAction(bookName, "viewByName");
                data.set(bookName, book.getBookId() + ":" + locale);
            }
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("application.keywords", locale);
        this.setText("application.description", locale);
        this.setText("application.title", locale);
        this.setText("application.language.name", locale);

        this.setText("page.welcome.caption", locale);
        this.setText("page.language-setting.title", locale);
        this.setText("page.logout.caption", locale);
        this.setText("page.reading.title", locale);

        this.setText("navigator.bible.caption", locale);
        this.setText("navigator.video.caption", locale);
        this.setText("navigator.document.caption", locale);
        this.setText("navigator.reader.caption", locale);
        this.setText("navigator.controller.caption", locale);
        this.setText("navigator.help.caption", locale);

        this.setText("holy.book.forward", locale);
        this.setText("holy.book.previous", locale);
        this.setText("holy.book.next", locale);
        this.setText("holy.bible", locale);
        this.setText("holy.bible.old-testament", locale);
        this.setText("holy.bible.new-testament", locale);
        this.setText("holy.bible.directory", locale);
        this.setText("holy.book.find-and-reading", locale);
        this.setText("holy.book.tools", locale);
        this.setText("holy.bible.version", locale);

        this.setText("footer.report-a-site-bug", locale);
        this.setText("footer.privacy", locale);
        this.setText("footer.register", locale);
        this.setText("footer.api", locale);
        this.setText("footer.updates-rss", locale);
        this.setText("holy.book.select", locale);

        this.setText("search.confirm.caption", locale);
        this.setText("search.submit.caption", locale);

        this.setText("invite.confirm.caption", locale);
        this.setText("invite.submit.caption", locale);
        this.setText("invite.email.default.tips", locale);

        this.setText("subscribe.plan", locale);
        this.setText("subscribe.bible.plan", locale);
        this.setText("subscribe.article.plan", locale);
        this.setText("subscribe.submit.caption", locale);
        this.setText("subscribe.email.caption", locale);
        this.setText("subscribe.email.default.tips", locale);

        this.setText("user.lastlogin.caption", locale);
        this.setText("holy.bible.download", locale);
        this.setText("holy.bible.chinese.download", locale);

        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setText("page.welcome.hello", (username == null || username.trim().isEmpty()) ? "" : username + "，", locale);

        this.setVariable("TEMPLATES_DIR", "/themes");

        this.setVariable("keyword", SharedVariables.getInstance(getLocale().toString()).getVariable("keyword") == null ? "" : SharedVariables.getInstance(getLocale().toString()).getVariable("keyword").getValue().toString());
        this.setVariable("metas", "");
    }

    @Override
    public String version() {
        return null;
    }

    public Object viewByName() throws ApplicationException {
        return this.viewByName(1);
    }

    public Object viewByName(int chapterId) throws ApplicationException {
        return this.viewByName(chapterId, 0);
    }

    public Object viewByName(int chapterId, int partId) throws ApplicationException {
        String bookName = (String) getContext().getAttribute("REQUEST_PATH");

        if (bookName.indexOf('/') != -1)
            bookName = bookName.split("/")[0];

        if (data.get(bookName) != null) {
            String localeBook = data.get(bookName).toString();
            if(localeBook.indexOf(':')!=-1) {
                String[] book = localeBook.split(":");
                int bookId = Integer.parseInt(book[0]);
                this.setLocale(book[1]);
                return this.read(bookId, chapterId, partId);
            }
        }

        Response response = (Response) getContext().getAttribute(HTTP_RESPONSE);
        response.setStatus(ResponseStatus.NOT_FOUND);

        return response;
    }

    @Action("bible")
    public Object read() throws ApplicationException {
        return this.read(1);
    }

    @Action("bible")
    public Object read(int bookId) throws ApplicationException {
        return this.read(bookId, 1);
    }

    @Action("bible")
    public Object read(int bookId, int chapterId) throws ApplicationException {
        return this.read(bookId, chapterId, 0);
    }

    @Action("bible")
    public Object read(int bookId, int chapterId, int partId) throws ApplicationException {
        if (bookId == 0) bookId = 1;
        if (chapterId == 0) chapterId = 1;

        Request request = (Request) getContext().getAttribute(HTTP_REQUEST);
        book book = new book();

        String lang = this.getLocale().toString();
        if (lang.equalsIgnoreCase("en_GB")) {
            lang = "en_US";
        }

        String book_meta_key = "book:" + bookId + ":lang:" + lang;
        if (data.get(book_meta_key) != null) {
            book = (book) data.get(book_meta_key);
        } else {
            Table table = book.findWith("WHERE book_id=? AND language=?",
                    new Object[]{bookId, lang});
            if (!table.isEmpty()) {
                Row row = table.get(0);
                book.setData(row);
            }

            data.set(book_meta_key, book);
        }

        this.setVariable(new DataVariable("book", book), true);

        String host = String.valueOf(getContext().getAttribute("HTTP_HOST"));
        // remove the default language for action
        this.setVariable("action", host.substring(0, host.lastIndexOf("/")) + "/?q=" + getContext().getAttribute("REQUEST_PATH").toString());
        this.setVariable("base_url", String.valueOf(getContext().getAttribute("HTTP_HOST")));

        Session session = request.getSession(); //@TODO
        if (session.getAttribute("usr") != null) {
            User usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + usr.getEmail() + "</a>");
            this.setVariable("scripts", "$.ajax({url:\"" + this.getLink("services/getwords") + "\",dataType:\"xml\",type:'GET'}).success(function(data){data=wordsXML(data);ldialog.show(data);});");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
            this.setVariable("scripts", "");
        }

        bible bible = new bible();
        bible.setTableName("zh_CN");
        int max_chapter;

        String max_chapter_key = "book:" + bookId + ":max_chapter";
        if (data.get(max_chapter_key) != null) {
            max_chapter = (int) (data.get(max_chapter_key));
        } else {
            max_chapter = bible.setRequestFields("max(chapter_id) as max_chapter").findWith("WHERE book_id=?",
                    new Object[]{bookId}).get(0).get(0).get("max_chapter").intValue();
            data.set(max_chapter_key, max_chapter);
        }

        if (chapterId > max_chapter) chapterId = max_chapter;

        int lastchapterid = chapterId - 1 <= 0 ? 1 : chapterId - 1;
        int nextchapterid = Math.min(chapterId + 1, max_chapter);

        this.setVariable("chapterid", String.valueOf(chapterId));
        this.setVariable("partid", String.valueOf(partId));
        this.setVariable("maxchapter", String.valueOf(max_chapter));
        this.setVariable("lastchapter", String.valueOf(lastchapterid));
        this.setVariable("nextchapter", String.valueOf(nextchapterid));

        String chinese = "zh-CN";
        if (lang.equalsIgnoreCase("zh_TW")) {
            chinese = "zh-TW";
            bible.setTableName("zh_TW");
        }

        if (this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
            bible.setTableName("NIV");
            this.setVariable("version", "NIV");
            this.setVariable("language.switch", "<a href=\"?lang=" + chinese + "&version=CUV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">中文</a> | <a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-GB&version=KJV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">KJV</a>");
        } else if (this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
            bible.setTableName("KJV");
            this.setVariable("version", "KJV");
            this.setVariable("language.switch", "<a href=\"?lang=" + chinese + "&version=CUV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">中文</a> | <a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-US&version=NIV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">NIV</a>");
        } else {
            this.setVariable("version", "");
            this.setVariable("language.switch", "<a href=\"?lang=en-US&version=NIV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">NIV</a> | <a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-GB&version=KJV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">KJV</a>");
        }

        if (request.getParameter("version") != null && !request.getParameter("version").isEmpty()) {
            switch (request.getParameter("version")) {
                case "NIV":
                    bible.setTableName("NIV");
                    this.setVariable("version", "NIV");
                    this.setVariable("language.switch", "<a href=\"?lang=" + chinese + "&version=CUV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">中文</a> | <a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-GB&version=KJV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">KJV</a>");
                    break;
                case "ESV":
                    bible.setTableName("ESV");
                    this.setVariable("version", "ESV");
                    this.setVariable("language.switch", "<a href=\"?lang=" + chinese + "&version=CUV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">中文</a> | <a href=\"?lang=en-US&version=NIV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">NIV</a> | <a href=\"?lang=en-GB&version=KJV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">KJV</a>");
                    break;
                case "KJV":
                    bible.setTableName("KJV");
                    this.setVariable("version", "KJV");
                    this.setVariable("language.switch", "<a href=\"?lang=" + chinese + "&version=CUV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">中文</a> | <a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-GB&version=NIV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">NIV</a>");
                    break;
                default:
                    this.setVariable("version", "");
                    this.setVariable("language.switch", "<a href=\"?lang=en-GB&version=ESV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">ESV</a> | <a href=\"?lang=en-US&version=NIV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">NIV</a> | <a href=\"?lang=en-GB&version=KJV&q=bible/" + bookId + "/" + chapterId + "/" + partId + "#up\">KJV</a>");

                    break;
            }
        }

        String where = "WHERE book_id=? and chapter_id=? order by part_id";
        Table vtable = bible.setRequestFields("*").findWith(where, new Object[]{bookId, chapterId});
        int count = vtable.size();
        StringBuilder left_column = new StringBuilder();
        String line;

        if (count > 0) {
            int i;

            left_column.append("<ol>");

            for (i = 0; i < count; i++) {
                Row item = vtable.get(i);
                bible.setData(item);

                line = bible.getContent();

                if (line != null) {
                    if (i == 0 && !line.trim().isEmpty())
                        line = "<span class='firstletter'>" + line.charAt(0)
                                + "</span>" + line.substring(1);

                    line = line.replaceAll("\n\n", "<br />");
                    left_column.append("<li").append(partId == bible.getPartId() ? " class=\"selected\""
                            : "").append("><a class=\"sup\" onmousedown=\"rightMenu.show(event,'").append(bible.getId()).append("')\">").append(bible.getPartId()).append("</a>").append(line).append("</li>");
                }

            }

            left_column.append("</ol>");
        } else {
            left_column.append("暂时没有任何内容");
        }

        this.setVariable("left_column", left_column.toString());

        return this;
    }

    public Object menu(int bookid) throws ApplicationException {

        int i = 0;
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

    @Action("bible/feed")
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
        SimpleDateFormat format = new SimpleDateFormat("MM/dd"), full_format = new SimpleDateFormat("yyyy-MM-dd");
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

        book book = new book();
        try {
            String lang = this.getLocale().toString();
            if (this.getLocale().toString().equalsIgnoreCase("en_GB")) {
                lang = "en_US";
            }

            Table table = book.findWith("WHERE book_id=? and language=?",
                    new Object[]{bookId, lang});

            if (table.size() > 0) {
                Row row = table.get(0);
                book.setData(row);
            }
        } catch (ApplicationException e) {
            e.printStackTrace();
        }

        Element item = new Element("item");
        Element item_title = new Element("title");
        item_title.setData("<![CDATA[" + book.getBookName() + " " + chapterId + "]]>");
        item.addElement(item_title);

        Element item_link = new Element("link");
        item_link.setData(this.getLink("bible").replace("&", "&amp;") + "/" + format.format(new Date()));
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
        guid.setData(this.getLink("feed").replace("&", "&amp;") + "/"
                + format.format(new Date()));
        item.addElement(guid);

        // start
        StringBuilder buffer = new StringBuilder();
        String finded;
        buffer.append("<ol style=\"list-style-type: none;\">");

        String condition;
        if (chapterId == 0) {
            condition = "book_id=" + bookId;
        } else {
            condition = "book_id=" + bookId + " and chapter_id="
                    + chapterId;
        }

        String where = "WHERE " + condition + " order by part_id";
        bible bible = new bible();

        if (this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString()))
            bible.setTableName("ESV");
        else if (this.getLocale().toString().equalsIgnoreCase(Locale.US.toString()))
            bible.setTableName("NIV");
        else
            bible.setTableName(this.getLocale().toString());

        Table vtable = bible.findWith(where, new Object[]{});
        int count = vtable.size();
        if (count > 0) {
            Field fields;
            for (Row row : vtable) {
                for (Field field : row) {
                    fields = field;
                    finded = fields.get("content").value().toString();
                    buffer.append("<li><a class=\"sup\">").append(fields.get("part_id").intValue()).append("</a>").append(finded).append("</li>");
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
                + buffer + "]]>");
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

        Response response = (Response) getContext()
                .getAttribute(HTTP_RESPONSE);

//        this.response.setContentType("text/xml;charset=" + getConfiguration().get("charset"));
        response.headers().add(Header.CONTENT_TYPE.set(getConfiguration().get(Header.StandardValue.CHARSET.name())));
        return "<?xml version=\"1.0\" encoding=\"" + getConfiguration().get("charset") + "\"?>\r\n" +
                root;
    }

    public Object bible() throws ApplicationException {
        return this.bible(1);
    }

    public Object bible(int bookid) throws ApplicationException {
        return this.bible(bookid, 1);
    }

    public Object bible(int bookid, int chapterid) throws ApplicationException {
        return this.bible(bookid, chapterid, 0);
    }

    public Object bible(int bookId, int chapterId, int partId) throws ApplicationException {
        StringBuilder xml = new StringBuilder();
        String finded;

        book book = new book();
        try {
            String lang = this.getLocale().toString();
            if (this.getLocale().toString().equalsIgnoreCase("en_GB")) {
                lang = "en_US";
            }

            Table table = book.findWith("WHERE book_id=? and language=?",
                    new Object[]{bookId, lang});

            if (table.size() > 0) {
                Row row = table.get(0);
                book.setData(row);
            }
        } catch (ApplicationException e) {
            e.printStackTrace();
        }

        String condition;

        if (chapterId == 0) {
            condition = "book_id=" + bookId;
        } else {
            condition = "book_id=" + bookId + " and chapter_id="
                    + chapterId;
        }

        String where = "WHERE " + condition + " order by part_id";

        bible bible = new bible();

        if (this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString()))
            bible.setTableName("ESV");
        else if (this.getLocale().toString().equalsIgnoreCase(Locale.US.toString()))
            bible.setTableName("NIV");
        else
            bible.setTableName(this.getLocale().toString());

        Table vtable = bible.findWith(where, new Object[]{});
        int max_chapter = bible.setRequestFields(
                        "max(chapter_id) as max_chapter").findWith("WHERE book_id=?",
                        new Object[]{bookId}).get(0).get(0).get("max_chapter")
                .intValue();

        this.setVariable("maxchapter", String.valueOf(max_chapter));

        xml.append("<?xml version=\"1.0\" encoding=\"").append(getContext().getAttribute("charset")).append("\"?>");
        int lastchapterId = chapterId - 1 <= 0 ? 1 : chapterId - 1;
        int nextchapterId = Math.min(chapterId + 1, max_chapter);
        xml.append("<book id=\"book\" name=\"book\" bookid=\"").append(bookId).append("\" bookname=\"").append(book.getBookName()).append("\" chapterid=\"").append(chapterId).append("\" maxchapter=\"").append(max_chapter).append("\" lastchapter=\"").append(lastchapterId).append("\" nextchapter=\"").append(nextchapterId).append("\">\r\n");
        Field fields;
        for (Row row : vtable) {
            for (Field field : row) {
                fields = field;
                finded = fields.get("content").value().toString();
                if (partId == Integer.parseInt(fields.get("part_id")
                        .value().toString())) {
                    xml.append("<item uid=\"").append(fields.get("id").value().toString()).append("\" id=\"").append(fields.get("part_id").value().toString()).append("\" selected=\"true\">").append(finded).append("</item>");
                } else {
                    xml.append("<item uid=\"").append(fields.get("id").value().toString()).append("\" id=\"").append(fields.get("part_id").value().toString()).append("\" selected=\"false\">").append(finded).append("</item>");
                }
            }
        }
        xml.append("</book>");

        return xml.toString();
    }

    @Action("bible/api")
    public Object api() throws ApplicationException {
        boolean valid = false;

        Request request = (Request) getContext().getAttribute(HTTP_REQUEST);
        Response response = (Response) getContext().getAttribute(HTTP_RESPONSE);

        String s = "Basic realm=\"Login for Bible API\"";
//        response.setHeader("WWW-Authenticate", s);
        response.headers().add(Header.WWW_AUTHENTICATE.set(s));

        // Get the Authorization header, if one was supplied
        Object authHeader = request.headers().get(Header.AUTHORIZATION);
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader.toString());
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                // We only handle HTTP Basic authentication

                if (basic.equalsIgnoreCase("Basic")) {
                    String credentials = st.nextToken();

                    // This example uses sun.misc.* classes.
                    // You will need to provide your own
                    // if you are not comfortable with that.

                    String userPass = new String(Base64.getDecoder().decode(credentials));
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
                            Authentication context = new Authentication() {
                                @Override
                                public Object grant() {
                                    return null;
                                }

                                @Override
                                public boolean approved() {
                                    return userID.equals("Mover") && password.equals("ingod.asia");
                                }

                                @Override
                                public void identify(Credential credential, Map<String, Object> map) throws ApplicationException {

                                }

                            };
                            valid = context.approved();
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
            response.headers().add(Header.WWW_AUTHENTICATE.set("Basic realm=\"Login for Bible API\""));
            response.setStatus(ResponseStatus.UNAUTHORIZED);

            return "Forbidden! Authentication failed!";
        }

        return this.feed();
    }

}