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
import custom.objects.keyword;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Field;
import org.tinystruct.data.component.Pager;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Document;
import org.tinystruct.dom.Element;
import org.tinystruct.handler.Reforward;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.Session;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.util.StringUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class search extends AbstractApplication {
    private static int i = 0;
    private static String API_KEY = "AIzaSyCgMMCOs8drxcnBclraPiR0eU29qSF1vHM";
    private static String CUSTOM_SEARCH_ENGINE_ID = "016436735745445346824:fgyqgo18wfm";
    private final String[] ids = new String[]{"016436735745445346824:fgyqgo18wfm", "014099384324434647311:udrkfx4-ipk"};
    private final String[] keys = new String[]{"AIzaSyCgMMCOs8drxcnBclraPiR0eU29qSF1vHM", "AIzaSyC-k_Cm_xClsqzeOGk8Dh5ECaZ449Vf6Ic"};
    private Request request;
    private Response response;
    private User usr;

    @Override
    public void init() {
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("application.title");
        this.setText("application.language.name");

        this.setText("page.search.title");
        this.setText("page.welcome.caption");
        this.setText("page.language-setting.title");
        this.setText("page.logout.caption");
        this.setText("page.reading.title");

        this.setText("navigator.bible.caption");
        this.setText("navigator.video.caption");
        this.setText("navigator.document.caption");
        this.setText("navigator.reader.caption");
        this.setText("navigator.controller.caption");
        this.setText("navigator.help.caption");

        this.setText("holy.book.forward");
        this.setText("holy.book.previous");
        this.setText("holy.book.next");
        this.setText("holy.book.find-and-reading");
        this.setText("holy.book.tools");
        this.setText("holy.book.select");

        this.setText("holy.bible");
        this.setText("holy.bible.old-testament");
        this.setText("holy.bible.new-testament");

        this.setText("footer.report-a-site-bug");
        this.setText("footer.privacy");
        this.setText("footer.register");
        this.setText("footer.api");
        this.setText("footer.updates-rss");

        this.setText("search.confirm.caption");
        this.setText("search.submit.caption");
        this.setText("search.strict.mode");
        this.setText("search.advanced.mode");

        this.setText("invite.confirm.caption");
        this.setText("invite.submit.caption");

        this.setText("subscribe.plan");
        this.setText("subscribe.bible.plan");
        this.setText("subscribe.article.plan");
        this.setText("subscribe.submit.caption");
        this.setText("subscribe.email.caption");
        this.setText("user.lastlogin.caption");
        this.setText("holy.bible.download");
        this.setText("holy.bible.chinese.download");
        this.setText("search.info", 0, 0, "", 0);

        String username = "";
        if (this.getVariable("username") != null) {
            username = String.valueOf(this.getVariable("username").getValue());
        }

        this.setText("page.welcome.hello", (username == null || username.trim().isEmpty()) ? "" : username + "，");

        this.setVariable("TEMPLATES_DIR", "/themes");
        this.setVariable("keyword", "");
        this.setVariable("start", "0");
        this.setVariable("end", "0");
        this.setVariable("size", "0");
        this.setVariable("value", "");
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    @Action("bible/search")
    public Object query() throws ApplicationException {
        this.request = (Request) getContext()
                .getAttribute(HTTP_REQUEST);

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

        if (this.request.getParameter("keyword") != null)
            return this.query(this.request.getParameter("keyword"));

        return this;
    }

    public Object query(String query) throws ApplicationException {
        StringBuilder html = new StringBuilder();
        String[] keywords;

        int page = 1, pageSize = 20;

        this.request = (Request) getContext()
                .getAttribute(HTTP_REQUEST);
        if (this.request.getParameter("page") == null
                || this.request.getParameter("page").trim().isEmpty()) {
            page = 1;
        } else {
            page = Integer.parseInt(this.request.getParameter("page").toString());
        }

        int startIndex = (page - 1) * pageSize;
        this.setVariable("search.title", "无相关结果 - ");

        if (!query.trim().isEmpty()) {
            query = StringUtilities.htmlSpecialChars(query);
            if (query.indexOf('|') != -1) {
                String[] q = query.split("|");
                query = q[0];
            }

            query = query.trim().replaceAll("%|_", "");
            keywords = query.split(" ");

            this.setSharedVariable("keyword", query);
            this.setVariable("search.title", query + " - ");
            this.setVariable("keyword", query);
        } else {
            this.setSharedVariable("keyword", "");
            this.setVariable("keyword", "");
            return this;
        }

        StringBuilder condition = new StringBuilder();
        int i = 0, j, k = 0;
        String[] _keywords = new String[keywords.length];
        while (i < keywords.length) {
            _keywords[i] = "%" + keywords[i] + "%";
            if (condition.length() == 0) {
                condition.append(" bible.content like ? ");
            } else {
                condition.append(" AND bible.content like ? ");
            }
            i++;
        }

        Locale locale = this.getLocale();
        String lang = locale.toLanguageTag();
        if (condition.length() == 0)
            condition.append(" book.language='").append(locale).append("' ");
        else
            condition.append(" AND book.language='").append(locale).append("' ");

        book book = new book();
        bible bible = new bible();
        if (locale.toString().equalsIgnoreCase(Locale.US.toString())) {
            bible.setTableName("NIV");
        } else if (locale.toString().equalsIgnoreCase(Locale.UK.toString())) {
            bible.setTableName("ESV");
        } else {
            bible.setTableName(locale.toString());
        }

        String SQL = "SELECT bible.book_id, bible.chapter_id, bible.part_id, bible.content, book.book_name FROM " + bible.getTableName()
                + " as bible left join " + book.getTableName()
                + " as book on bible.book_id=book.book_id where " + condition
                + " order by bible.book_id,bible.chapter_id,bible.part_id limit " + startIndex + ","
                + pageSize;

        String look = "SELECT count(bible.id) AS size FROM " + bible.getTableName()
                + " as bible left join " + book.getTableName()
                + " as book on bible.book_id=book.book_id where " + condition;

        Table vtable = bible.find(SQL, _keywords);
        boolean noResult = !vtable.isEmpty();

        if (!noResult && !query.isEmpty()) {
            try {
                Table list = book.findWith("WHERE language=? and book_name=?",
                        new Object[]{this.getLocale().toString(), query});
                if (!list.isEmpty()) {
                    this.response = (Response) getContext()
                            .getAttribute(HTTP_RESPONSE);

                    Reforward reforward = new Reforward(request, response);
                    query = URLEncoder.encode(query, StandardCharsets.UTF_8);
                    reforward.setDefault(getContext().getAttribute("HTTP_HOST") + query);
                    reforward.forward();
                    return reforward;
                }
            } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Row found = bible.findOne(look, _keywords);

        long startTime = System.currentTimeMillis();
        Pager pager = new Pager();
        pager.setPageSize(pageSize);
        pager.setCurrentPage(page);
        pager.setListSize(found.getFieldInfo("size").intValue());

        Field field;
        int next = pager.getStartIndex();// 此位置即为当前页的第一条记录的ID

        html.append("<ol class=\"searchresults\" start=\"").append(next).append("\">\r\n");

        String finded, word;
        Row row;
        Iterator<Row> table = vtable.iterator();
        int n = 0;
        while (table.hasNext()) {
            row = table.next();
            Iterator<Field> iterator = row.iterator();

            n++;
            while (iterator.hasNext()) {
                field = iterator.next();
                finded = field.get("content").value().toString();

                j = 0;
                while (j < keywords.length) {
                    finded = StringUtilities.sign(finded, keywords[j++]);
                }

                html.append("<li").append(n % 2 == 0 ? " class=\"even\"" : " class=\"odd\"").append("><a href=\"").append(getContext().getAttribute("HTTP_HOST")).append("bible/").append(field.get("book_id").value().toString()).append("/").append(field.get("chapter_id").value().toString()).append("/").append(field.get("part_id").value().toString()).append("\" target=\"_blank\">").append(this.setText("search.bible.info", field.get("book_name").value()
                        .toString(), field.get("chapter_id").value().toString(), field
                        .get("part_id").value().toString())).append("</a><p>").append(finded).append("</p></li> \r\n");
                next++;
            }
        }

        Table ktable;
        Row krow;
        while (k < keywords.length && noResult) {
            word = keywords[k++];
            keyword keyword = new keyword();
            keyword.setKeyword(word);
            ktable = keyword.setRequestFields("id,visit").findWith("WHERE keyword=?",
                    new Object[]{word});

            if (ktable.isEmpty()) {
                keyword.setVisit(0);
                keyword.append();
            } else {
                krow = ktable.get(0);
                keyword.setId(krow.getFieldInfo("id").value());
                keyword.setVisit(krow.getFieldInfo("visit").intValue() + 1);
                keyword.update();
            }
        }
        html.append("</ol>\r\n");

        String actionURL = getContext().getAttribute("HTTP_HOST") + "bible/search/"
                + query + "&lang="+lang+"&page";
        pager.setFirstPageText(this.getProperty("page.first.text"));
        pager.setLastPageText(this.getProperty("page.last.text"));
        pager.setCurrentPageText(this.getProperty("page.current.text"));
        pager.setNextPageText(this.getProperty("page.next.text"));
        pager.setEndPageText(this.getProperty("page.end.text"));
        pager.setControlBarText(this.getProperty("page.controlbar.text"));

        html.append("<div class=\"pagination\" style=\"cursor:default\">").append(pager.getPageControlBar(actionURL)).append("</div>\r\n");
        html.append("<!-- ").append(System.currentTimeMillis() - startTime).append(" -->");

        int start = page - 1 == 0 ? 1 : (page - 1) * pageSize + 1, end = page
                * pageSize;

        this.setVariable("start", String.valueOf(start));
        this.setVariable("end", String.valueOf(end));
        this.setVariable("size", String.valueOf(pager.getSize()));
        this.setVariable("value", html.toString());
        this.setVariable("action", getConfiguration().get("default.base_url")
                + getContext().getAttribute("REQUEST_PATH").toString());

        this.setText("search.info", start, end, query, pager.getSize());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            this.usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile",
                    "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"
                            + this.usr.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login")
                    + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    public String feed(String query) throws ApplicationException {
        StringBuilder xml = new StringBuilder();
        String finded = "";
        String[] keywords;
        boolean noResult = true;

        int page = 1, pageSize = 20;
        if (this.request.getParameter("page") == null
                || this.request.getParameter("page").trim().length() <= 0) {
        } else {
            page = Integer.parseInt(this.request.getParameter("page"));
        }

        int startIndex = (page - 1) * pageSize;
        if (!query.trim().isEmpty()) {
            keywords = query.split(" ");
        } else {
            return "<result>Error</result>";
        }

        String condition = "";
        for (String s : keywords) {
            if (condition.trim().isEmpty()) {
                condition = " content like '%" + s + "%' ";
            } else {
                condition += " or content like '%" + s + "%' ";
            }
        }

        String SQL = "SELECT a.*,b.book_name FROM bible as a left join book as b on a.book_id=b.book_id where "
                + condition
                + " order by a.book_id,a.chapter_id limit "
                + startIndex
                + "," + pageSize;
        // String look = "SELECT FOUND_ROWS() AS size";

        bible bible = new bible();
        Table vtable = bible.find(SQL, new Object[]{});
        noResult = !vtable.isEmpty();

        Field field;
        int next = startIndex + 1;// 此位置即为当前页的第一条记录的ID

        for (Row row : vtable) {
            for (Field value : row) {
                field = value;
                finded = field.get("content").value().toString();

                for (int j = 0; j < keywords.length; j++) {
                    finded = StringUtilities.sign(finded, keywords[j]);
                }

                xml.append("<item id=\"").append(next).append("\" chapterid=\"").append(field.get("chapter_id").value().toString()).append("\" bookid=\"").append(field.get("book_id").value().toString()).append("\" ").append(field.get("book_name").value().toString()).append(" partid=\"").append(field.get("part_id").value().toString()).append("\">").append(finded).append("</item>\r\n");
                next++;
            }
        }

        for (int k = 0; k < keywords.length && noResult; k++) {
            keyword keyword = new keyword();
            keyword.setKeyword(keywords[k]);
            Row findRow = keyword.findOne(
                    "SELECT id,visit FROM keyword WHERE keyword='" + keywords[k] + "'",
                    new Object[]{});

            if (findRow.isEmpty()) {
                keyword.setVisit(0);
                keyword.append();
            } else {
                keyword.setId(findRow.getFieldInfo("id"));
                keyword.setVisit(findRow.getFieldInfo("visit").intValue() + 1);
                keyword.update();
            }
        }

        return xml.toString();
    }

    @Action("bible/advsearch")
    public Object advanced(String query) throws ApplicationException {
        if (query == null || query.trim().isEmpty()) {
            return this;
        }
        query = StringUtilities.htmlSpecialChars(query);

        int page = 1, pageSize = 10, total = 0;
        this.request = (Request) getContext().getAttribute(HTTP_REQUEST);

        if (this.request.getParameter("page") == null
                || this.request.getParameter("page").trim().isEmpty()) {
            page = 1;
        } else {
            page = Integer.parseInt(this.request.getParameter("page").toString());
        }

        if (this.request.getParameter("amount") == null || this.request.getParameter("amount").toString().trim().length() == 0) {
            total = 1;
        } else {
            total = Integer.parseInt(this.request.getParameter("amount").toString());
        }

        long startTime = System.currentTimeMillis();
        Pager pager = new Pager();
        pager.setPageSize(pageSize);
        pager.setCurrentPage(page);
        pager.setListSize(total);

        if (query == null || !query.isEmpty()) {
            this.setVariable("keyword", "");
        } else {
            this.setVariable("keyword", query);
            this.setVariable("search.title", query + " - ");
        }

        Document document = this.execute(query, pager.getStartIndex());
        Element root = document.getRoot();
        List<Element> vtable = root.getElementsByTagName("entry");
        if (vtable.isEmpty()) {
            this.setVariable("value", "Sorry, we could not get any related results with this keyword! " + StringUtilities.htmlSpecialChars(root.toString()));
            return this;
        }

        int n = 0, next, amount = Integer.parseInt(root.getElementsByTagName("opensearch:totalResults").get(0).getData());
        pager.setListSize(amount);

        next = pager.getStartIndex();// 此位置即为当前页的第一条记录的ID
        // opensearch:totalResults
        StringBuilder html = new StringBuilder();
        html.append("<ol class=\"searchresults\" start=\"").append(next).append("\">\r\n");

        Element element, title, link;
        List<Element> t;
        String summary;

        for (Element value : vtable) {
            element = value;
            n++;
            link = element.getElementsByTagName("id").get(0);
            title = element.getElementsByTagName("title").get(0);

            t = element.getElementsByTagName("cse:PageMap").get(0)
                    .getElementsByTagName("cse:DataObject");
            if (t.size() >= 3) {
                t = t.get(1).getElementsByTagName("cse:Attribute");
                summary = t.get(1).getAttribute("value");
            } else
                summary = element.getElementsByTagName("summary").get(0).getData();

            html.append("<li").append(n % 2 == 0 ? " class=\"even\"" : " class=\"odd\"").append("><a href=\"").append(link.getData()).append("\" target=\"_blank\">").append(title.getData()).append(" </a><p>").append(summary).append("</p></li> \r\n");
            next++;
        }

        html.append("</ol>\r\n");

        String actionURL = getContext().getAttribute("HTTP_HOST") + "bible/advsearch/" + query + "&amount=" + amount + "&page";
        pager.setFirstPageText(this.getProperty("page.first.text"));
        pager.setLastPageText(this.getProperty("page.last.text"));
        pager.setCurrentPageText(this.getProperty("page.current.text"));
        pager.setNextPageText(this.getProperty("page.next.text"));
        pager.setEndPageText(this.getProperty("page.end.text"));
        pager.setControlBarText(this.getProperty("page.controlbar.text"));

        html.append("<div class=\"pagination\" style=\"cursor:default\">").append(pager.getPageControlBar(actionURL)).append("</div>\r\n");
        html.append("<!-- ").append(System.currentTimeMillis() - startTime).append(" -->");

        int start = page - 1 == 0 ? 1 : (page - 1) * pageSize + 1, end = page
                * pageSize;

        this.setVariable("start", String.valueOf(start));
        this.setVariable("end", String.valueOf(end));
        this.setVariable("size", String.valueOf(pager.getSize()));
        this.setVariable("value", html.toString());

        this.setText("search.info", start, end, query, pager.getSize());

        this.setVariable("action", getContext().getAttribute("HTTP_HOST") + getContext().getAttribute("REQUEST_PATH").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            this.usr = (User) session.getAttribute("usr");

            this.setVariable("user.status", "");
            this.setVariable("user.profile",
                    "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">"
                            + this.usr.getEmail() + "</a>");
        } else {
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login")
                    + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");
        }

        return this;
    }

    protected String createRequestString(String query, int start)
            throws UnsupportedEncodingException {
        String encoded_query = URLEncoder.encode(query, StandardCharsets.UTF_8);

        return "https://www.googleapis.com/customsearch/v1?" +
                "key=" + API_KEY +
                "&cx=" + CUSTOM_SEARCH_ENGINE_ID +
                "&q=" + encoded_query +
                "&alt=atom" +
                "&start=" + start;
    }

    private Document execute(String query, int start) throws ApplicationException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget;
        try {
            httpget = new HttpGet(createRequestString(query, start == 0 ? 1 : start));
            httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");

            HttpResponse response = httpClient.execute(httpget);
            InputStream instream = response.getEntity().getContent();

            Document document = new Document();
            document.load(instream);
            if (document.getRoot().getElementsByTagName("errors").size() > 0) {
                if (i++ > ids.length - 1) i = 0;

                CUSTOM_SEARCH_ENGINE_ID = ids[i];
                API_KEY = keys[i];

                httpget = new HttpGet(createRequestString(query, start == 0 ? 1 : start));

                response = httpClient.execute(httpget);
                instream = response.getEntity().getContent();

                document.load(instream);
            }

            return document;
        } catch (ClientProtocolException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

    }
}
