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
import custom.objects.report;
import custom.objects.vocabulary;
import custom.util.ActivationKey;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Document;
import org.tinystruct.dom.Element;
import org.tinystruct.http.Reforward;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.Session;
import org.tinystruct.mail.SimpleMail;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.util.URLResourceLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class sender extends AbstractApplication {
    private Reforward reforward;

    @Action("services/report")
    public boolean send(Request request) throws ApplicationException {
        if (request.getParameter("id") == null
                || request.getParameter("text") == null
                || request.getParameter("text").trim().isEmpty()) {
            return false;
        }

        report report = new report();
        report.setBibleId(request.getParameter("id"));
        report.setLanguageId(0);
        report.setUserId("-");
        report.setStatus(0);
        report.setUpdatedContent(request.getParameter("text"));

        report.setModifiedTime(new Date());
        try {
            Object id = report.appendAndGetId();

            bible bible = new bible();
            if (this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
                bible.setTableName("NIV");
            } else if (this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
                bible.setTableName("KJV");
            } else {
                bible.setTableName(this.getLocale().toString());
            }

            bible.setId(report.getBibleId());
            bible.findOneById();

            SimpleMail mail = new SimpleMail();
            mail.setFrom(this.getProperty("mail.default.from").toString());
            //你好！有一个用户发来的经文更新请求!<br />原文是：%s，建议更新为：%s <br/> --- <br />请点击此链接进行确认！(或复制此地址到浏览器访问):<br /> %s <br /><br /> InGod.asia工作小组
            String body = String.format(
                    this.getProperty("mail.report.content"), bible.getContent(), report.getUpdatedContent(),
                    getContext().getAttribute("HTTP_HOST") + "services/bible/update/" + id);
            mail.setSubject(this.getProperty("mail.report.title"));
            mail.setBody(body);
            mail.addTo("moverinfo@gmail.com");

            mail.send();

        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            throw new ApplicationException("Email send failed. please contact the administrator.", e);
        }

        return true;
    }

    @Action("friends/invite")
    public String invite(Request request) throws ApplicationException {
        Session session = request.getSession();
        User user;
        if (session.getAttribute("usr") != null) {
            user = (User) session.getAttribute("usr");
        } else return "false";

        String mailto = "moverinfo@gmail.com";
        if (request.getParameter("mailto") == null
                || request.getParameter("mailto").trim().isEmpty()) {
            return "false";
        } else
            mailto = request.getParameter("mailto");

        String[] addresses = mailto.split(";");

        for (String address : addresses) {
            if (address.indexOf('@') < 1
                    || address.indexOf('@') >= address
                    .lastIndexOf('.') + 1) {
                return "invalid";
            }
        }

        SimpleMail mail = new SimpleMail();
        mail.setFrom(this.getProperty("mail.default.from"));

        ActivationKey key = new ActivationKey();
        String randomKey = key.getRandomCode();

        String body = String.format(
                this.getProperty("mail.invitation.content"),
                this.getProperty("application.title"),
                this.getLink("user/register/" + randomKey));
        mail.setSubject(this.getProperty("mail.invitation.title"));
        mail.setBody(body);
        mail.setTo(mailto);

        if (!mail.send()) {
            return "error";
        }

        return "true";
    }

    @Action("services/bible/update")
    public boolean update(String id, Request request, Response response) throws ApplicationException {
        report report = new report();
        report.setId(id);
        report.findOneById();

        bible bible = new bible();

        if (this.getLocale().toString().equalsIgnoreCase(Locale.US.toString())) {
            bible.setTableName("NIV");
        } else if (this.getLocale().toString().equalsIgnoreCase(Locale.UK.toString())) {
            bible.setTableName("KJV");
        } else {
            bible.setTableName(this.getLocale().toString());
        }

        bible.setId(report.getBibleId());
        bible.setContent(report.getUpdatedContent());

        report.setStatus(1);

        if (bible.update() && report.update()) {
            bible.findOneById();

            this.reforward = new Reforward(request, response);
            this.reforward.setDefault(this.getLink("bible", null) + "/" + bible.getBookId() + "/" + bible.getChapterId() + "/" + bible.getPartId());
            this.reforward.forward();
            return true;
        }

        return false;
    }

    public void init() {
        this.setTemplateRequired(false);
    }

    @Action("services/getword")
    public String getWord(Request request, String word) throws MalformedURLException, ApplicationException {
        String url = "http://dict.youdao.com/fsearch?client=deskdict&keyfrom=chrome.extension&q=" + word.trim() + "&pos=-1&doctype=xml&vendor=unknown&appVer=3.1.17.4208&le=eng";
        User user;
        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            user = (User) session.getAttribute("usr");
            Document doc = new Document();
            doc.load(new URL(url));
            Element document = doc.getRoot();
            if (document.getChildNodes().size() < 3)
                throw new ApplicationException("The dictionary resource is temporarily unavailable");

            vocabulary vocabulary = new vocabulary();
            vocabulary.setUserId(user.getId());
            vocabulary.setDate(LocalDateTime.now());
            vocabulary.setReferenceLink(request.getParameter("referer"));

            List<Element> phrase = document.getElementsByTagName("return-phrase");
            if (!phrase.isEmpty()) {
                vocabulary.setWord(phrase.get(0).getData());
            }

            List<Element> phonetic_symbol = document.getElementsByTagName("phonetic-symbol");
            if (!phonetic_symbol.isEmpty()) {
                vocabulary.setPhoneticSymbol(phonetic_symbol.get(0).getData());
            }

            List<Element> custom_translation = document.getElementsByTagName("custom-translation");
            if (!custom_translation.isEmpty()) {
                StringBuilder buff = new StringBuilder();

                Iterator<Element> citerator = custom_translation.get(0).getElementsByTagName("translation").iterator();
                while (citerator.hasNext()) {
                    if (buff.length() > 0) buff.append("\r\n");
                    buff.append(citerator.next().getElementsByTagName("content").get(0).getData());
                }
                vocabulary.setInterpretation(buff.toString());
            }

            Table words = vocabulary.findWith("WHERE word=? and user_id=?", new Object[]{vocabulary.getWord(), user.getId()});
            if (words.isEmpty()) {
                vocabulary.append();
            } else {
                vocabulary.setData(words.get(0));
                vocabulary.setDate(LocalDateTime.now());
                vocabulary.update();
            }

            return document.toString();
        }

        URLResourceLoader loader = new URLResourceLoader(new URL(url));

        return loader.getContent().toString();
    }

    @Action("services/deleteword")
    public void deleteWord(Request request, Response response, String id) throws ApplicationException {
        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User user = (User) session.getAttribute("usr");
            vocabulary vocabulary = new vocabulary();
            vocabulary.setId(id);

            vocabulary.delete();

            this.reforward = new Reforward(request, response);
            this.reforward.setDefault(this.getLink("dashboard"));
            this.reforward.forward();
        } else throw new ApplicationException("Permission Denied");
    }

    @Action("services/getwords")
    public Object getAllWords(Request request) {
        Session session = request.getSession();
        if (session.getAttribute("usr") != null) {
            User user = (User) session.getAttribute("usr");
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            buffer.append("<list userId=\"").append(user.getId()).append("\">");
            vocabulary vocabulary = new vocabulary();
            try {
                Table list = vocabulary.findWith("WHERE user_id=? order by date desc limit 0,7", new Object[]{user.getId()});
                Iterator<Row> rows = list.iterator();
                while (rows.hasNext()) {
                    vocabulary.setData(rows.next());

                    buffer.append("<item word=\"").append(vocabulary.getWord()).append("\" phoneticSymbol=\"").append(vocabulary.getPhoneticSymbol()).append("\">").append(vocabulary.getInterpretation()).append("</item>");
                }
            } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            buffer.append("</list>");

            return buffer;
        }

        return null;
    }

    @Action("services/translate")
    public StringBuilder getTranslate(String words) throws MalformedURLException, ApplicationException {
        String url = "http://fanyi.youdao.com/translate?client=deskdict&keyfrom=chrome.extension&xmlVersion=1.1&dogVersion=1.0&ue=utf8&i=" + (words) + "&doctype=xml";

        URLResourceLoader loader = new URLResourceLoader(new URL(url));

        return loader.getContent();
    }

    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

}