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
import custom.objects.vocabulary;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Pager;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.handler.Reforward;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;

import static org.tinystruct.handler.DefaultHandler.HTTP_REQUEST;
import static org.tinystruct.handler.DefaultHandler.HTTP_RESPONSE;

public class dashboard extends AbstractApplication {

    private Request request;
    private User user;
    private Response response;

    @Override
    public void init() {
        // TODO Auto-generated method stub
        this.setAction("dashboard", "index");
        this.setAction("dashboard/profile", "condition");
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.setText("page.dashboard.title");
        this.setText("application.title");
        this.setText("application.language.name");

        this.setText("page.welcome.caption");
        this.setText("page.language-setting.title");
        this.setText("page.logout.caption");

        this.setText("words.list.title");

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

    public Object index() throws ApplicationException {
        this.request = (Request) this.context.getAttribute(HTTP_REQUEST);

        this.setVariable("action", String.valueOf(this.context.getAttribute("HTTP_HOST")) + this.context.getAttribute("REQUEST_ACTION").toString());

        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            this.user = (User) session.getAttribute("usr");
            this.setVariable("user.status", "");
            this.setVariable("user.profile", "<a href=\"javascript:void(0)\" onmousedown=\"profileMenu.show(event,'1')\">" + this.user.getEmail() + "</a>");
            this.setVariable("scripts", "$.ajax({url:\"" + this.getLink("services/getwords") + "\",dataType:\"xml\",type:'GET'}).success(function(data){data=wordsXML(data);ldialog.show(data);});");

            StringBuffer buffer = new StringBuffer();
            vocabulary vocabulary = new vocabulary();
            try {
                long startTime = System.currentTimeMillis();
                int page = 1, pageSize = 20;

                if (this.request.getParameter("page") == null || this.request.getParameter("page").toString().trim().length() <= 0) {
                    page = 1;
                } else {
                    page = Integer.parseInt(this.request.getParameter("page").toString());
                }

                Row found = vocabulary.findOne("SELECT count(user_id) AS size FROM " + vocabulary.getTableName() + " WHERE user_id=? ", new Object[]{this.user.getId()});

                int startIndex = (page - 1) * pageSize;
                Table list = vocabulary.findWith("WHERE user_id=? order by date desc limit " + startIndex + "," + pageSize, new Object[]{this.user.getId()});

                Pager pager = new Pager();
                pager.setPageSize(pageSize);
                pager.setCurrentPage(page);
                pager.setListSize(found.getFieldInfo("size").intValue());

                int next = pager.getStartIndex();//此位置即为当前页的第一条记录的ID

                buffer.append("<ol class=\"searchresults\" start=\"" + next + "\">\r\n");
                Iterator<Row> rows = list.iterator();

                SimpleDateFormat format = new SimpleDateFormat(this.getConfiguration("default.date.format"));
                while (rows.hasNext()) {
                    vocabulary.setData(rows.next());

                    buffer.append("<li" + (next % 2 == 0 ? " class=\"even\"" : " class=\"odd\"") + "><span class=\"phrase\">").append("<a href=\"" + (vocabulary.getReferenceLink() != null && vocabulary.getReferenceLink().trim().length() > 0 ? vocabulary.getReferenceLink() : "javascript:void(0)") + "\">").append(vocabulary.getWord()).append("</a>").append("</span>");
                    if (vocabulary.getPhoneticSymbol() != null && !vocabulary.getPhoneticSymbol().trim().isEmpty())
                        buffer.append("<span class=\"phonetic\">[").append(vocabulary.getPhoneticSymbol()).append("]</span>");
                    buffer.append(" <span class=\"date\">" + vocabulary.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "</span> <a href=\"" + this.getLink("services/deleteword") + "/" + vocabulary.getId() + "\" class=\"remove-button\">×</a>");
                    buffer.append("<span class=\"interpretation\">").append(vocabulary.getInterpretation()).append("</span></li>");
                    next++;
                }
                buffer.append("</ol>");

                String actionURL = this.getLink("dashboard") + "&page";
                pager.setFirstPageText(this.getProperty("page.first.text"));
                pager.setLastPageText(this.getProperty("page.last.text"));
                pager.setCurrentPageText(this.getProperty("page.current.text"));
                pager.setNextPageText(this.getProperty("page.next.text"));
                pager.setEndPageText(this.getProperty("page.end.text"));
                pager.setControlBarText(this.getProperty("page.controlbar.text"));

                buffer.append("<div class=\"pagination\" style=\"cursor:default\">" + pager.getPageControlBar(actionURL) + "</div>\r\n");
                buffer.append("<!-- " + String.valueOf(System.currentTimeMillis() - startTime) + " -->");

                int start = page - 1 == 0 ? 1 : (page - 1) * pageSize + 1, end = page * pageSize;

                this.setVariable("start", String.valueOf(start));
                this.setVariable("end", String.valueOf(end));
                this.setVariable("size", String.valueOf(pager.getSize()));
                this.setVariable("words.list", buffer.toString());
                this.setText("page.info", start, end, pager.getSize());

            } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            this.setVariable("words.list", "");
            this.setVariable("user.status", "<a href=\"" + this.getLink("user/login") + "\">" + this.getProperty("page.login.caption") + "</a>");
            this.setVariable("user.profile", "");

            this.response = (Response) this.context.getAttribute(HTTP_RESPONSE);

            Reforward reforward = new Reforward(request, response);
            reforward.setDefault(this.getLink(this.config.get("default.login.page")) + "&from=" + this.getLink("dashboard"));
            return reforward.forward();
        }

        this.setVariable("action", String.valueOf(this.context.getAttribute("HTTP_HOST")) + this.context.getAttribute("REQUEST_ACTION").toString());

        return this;
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}
