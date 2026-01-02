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

import custom.objects.bible;
import custom.objects.book;
import custom.objects.plan;
import custom.util.TaskDescriptor;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.data.DatabaseOperator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.tinystruct.dom.Element;
import org.tinystruct.http.Header;
import org.tinystruct.http.Response;
import org.tinystruct.http.ResponseHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class document extends AbstractApplication {

    private Response response;

    @Override
    public void init() {
        this.setAction("feed", "feed");
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        // TODO Auto-generated method stub
        this.setText("search.confirm.caption");
        this.setText("search.submit.caption");

        this.setText("invite.confirm.caption");
        this.setText("invite.submit.caption");
    }

    public String feed() throws ApplicationException {
        SimpleDateFormat format = new SimpleDateFormat("MM"), format1 = new SimpleDateFormat("dd");

        Date date = new Date();
        int month = Integer.parseInt(format.format(date));
        int day = Integer.parseInt(format1.format(date));
        return this.feed(month, day);
    }

    public String feed(int month, int day) throws ApplicationException {
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
        atom_link.setAttribute("href", this.getLink("bible").toString().replaceAll("&", "&amp;"));
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat format = new SimpleDateFormat("MM/dd"), full_format = new SimpleDateFormat("yyyy-MM-dd"),
                format1 = new SimpleDateFormat("MM-dd"), format2 = new SimpleDateFormat("yyyy-M-d");
        Element lastBuildDate = (Element) element.clone();
        lastBuildDate.setName("lastBuildDate");

        try {
            date = format2.parse(calendar.get(Calendar.YEAR) + "-" + month + "-" + day);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException(e.getMessage(), e.getCause());
        }

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

        Element item = new Element("item");
        Element item_title = new Element("title");
        item_title.setData("<![CDATA[每日读经 " + format.format(date) + "]]>");
        item.addElement(item_title);

        Element item_link = new Element("link");

        item_link.setData(this.getLink("feed").replace("&", "&amp;") + "/"
                + format.format(new Date()));
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

        plan plan = new plan();
        plan.findOneByKey("date", "2010-"
                + format1.format(date).toString());

        if (plan.getTask() == null) {
            throw new ApplicationException("Task is not ready!");
        }

        TaskDescriptor task = new TaskDescriptor();

        String tableName;
        String locale = getLocale().toString();
        if (locale.equalsIgnoreCase(Locale.UK.toString()))
            tableName = "KJV";
        else if (locale.equalsIgnoreCase(Locale.US.toString()))
            tableName = "NIV";
        else if (locale.equalsIgnoreCase(Locale.TAIWAN.toString()))
            tableName = "zh_TW";
        else
            tableName = "zh_CN";

        StringBuffer where = task.parse(plan.getTask());

        ArrayList<List<bible>> res = new ArrayList<List<bible>>();

        List<bible> bibles = new ArrayList<bible>();
        int bookId = 0, chapterId = 0;

        try (DatabaseOperator operator = new DatabaseOperator()) {
            operator.disableSafeCheck();
            String sql = "SELECT * FROM " + tableName + " WHERE " + where;
            PreparedStatement preparedStatement = operator.preparedStatement(sql, new Object[]{});
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                bible bib = new bible();
                bib.setBookId(resultSet.getInt("book_id"));
                bib.setChapterId(resultSet.getInt("chapter_id"));
                bib.setPartId(resultSet.getInt("part_id"));
                bib.setContent(resultSet.getString("content"));

                if (bookId != bib.getBookId()) {
                    bookId = bib.getBookId();

                    if (!bibles.isEmpty())
                        res.add(bibles);

                    bibles = new ArrayList<bible>();
                }

                bibles.add(bib);
            }
        } catch (SQLException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

        if (!bibles.isEmpty())
            res.add(bibles);

        StringBuffer buffer = new StringBuffer();
        Iterator<List<bible>> iterator = res.iterator();

        bookId = 0;
        book book = new book();
        while (iterator.hasNext()) {
            List<bible> bs = iterator.next();

            Table table = book.findWith("WHERE book_id=? and language=?",
                    new Object[]{bs.get(0).getBookId(),
                            this.getLocale().toString().equalsIgnoreCase(Locale.US.toString()) ? Locale.US.toString()
                                    : "zh_CN"});
            if (table.size() > 0) {
                Row row = table.get(0);
                book.setData(row);

                bookId = book.getBookId();
            }

            buffer.append("<h3><a href=\"").append(this.getLink("bible/" + book.getBookId())).append("\">").append(book.getBookName()).append("</a></h3>");

            Iterator<bible> iter = bs.iterator();
            chapterId = 0;
            while (iter.hasNext()) {

                bible bi = iter.next();

                if (chapterId != bi.getChapterId()) {
                    buffer.append("<h4><a href=\"").append(this.getLink("bible/" + book.getBookId() + "/"
                            + bi.getChapterId())).append("\">").append(bi.getChapterId()).append("章</a></h4>");
                    chapterId = bi.getChapterId();
                }

                buffer.append(" ").append(bi.getPartId()).append(" ").append(bi.getContent());
            }

        }

        Element item_description = (Element) element.clone();
        item_description.setName("description");
        item_description
                .setData("<![CDATA["
                        + buffer
                        + "<br /><a href=\"" + this.getLink("bible") + "\" style=\"float:right\">"
                        + this
                        .getProperty("subscribe.continue.caption")
                        + "</a>]]>");

        item.addElement(item_description);

        Element content_encoded = (Element) element.clone();
        content_encoded.setName("content:encoded");
        content_encoded.setData("<![CDATA["
                + buffer.toString() + "]]>");
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

        this.response = (Response) getContext()
                .getAttribute(HTTP_RESPONSE);
        ResponseHeaders headers = new ResponseHeaders(this.response);
        headers.add(Header.CONTENT_TYPE.set("text/xml;charset="
                + getConfiguration().get("charset")));

        return "<?xml version=\"1.0\" encoding=\""
                + getConfiguration().get("charset") + "\"?>\r\n" +
                root;
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}