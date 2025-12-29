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

import custom.objects.*;
import custom.util.TaskDescriptor;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.DatabaseOperator;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.dom.Element;
import org.tinystruct.mail.SimpleMail;
import org.tinystruct.system.ApplicationManager;
import org.tinystruct.system.Event;
import org.tinystruct.system.EventDispatcher;
import org.tinystruct.system.Resource;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.scheduling.Scheduler;
import org.tinystruct.system.scheduling.SchedulerTask;
import org.tinystruct.system.scheduling.TimeIterator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class dailymail extends AbstractApplication {

    private Scheduler scheduler;
    private suggestion suggestion;
    private final AtomicBoolean next = new AtomicBoolean(false);

    private void loadBible() {
        try {
            this.next.set(false);
            this.setText("application.title");
            String title = Resource.getInstance(getLocale()).getLocaleString("application.title");
            SimpleMail themail = new SimpleMail();
            themail.setFrom(title);

            SimpleDateFormat format = new SimpleDateFormat(
                    "MM-dd");
            plan plan = new plan();

            Date date = new Date();
            plan.findOneByKey("date", "2010-" + format.format(date));

            if (plan.getTask() == null) {
                throw new ApplicationException("Task is not ready!");
            }

            // start
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

            ArrayList<List<bible>> res = new ArrayList<List<bible>>();

            List<bible> bibles = new ArrayList<bible>();
            int bookId = 0, chapterId = 0;

            // Fetch the bible verses from database
            try (DatabaseOperator operator = new DatabaseOperator();) {
                operator.disableSafeCheck();
                StringBuffer where = task.parse(plan.getTask());
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

                        bibles = new ArrayList<>();
                    }

                    bibles.add(bib);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (!bibles.isEmpty())
                res.add(bibles);

            StringBuilder buffer = new StringBuilder();
            buffer.append(
                    "<div style=\"background: none repeat scroll 0 0 -moz-field; border: 1px solid threedshadow; margin: 2em auto; padding: 2em;\">");
            buffer.append("	<div>");
            buffer.append(
                    "		<a style=\"-moz-margin-end: 0; -moz-margin-start: 0.6em; float: right; margin-bottom: 0; margin-top: 0;\">");
            buffer.append(
                    "			<img id=\"feedTitleImage\" src=\"https://www.ingod.today/themes/images/favicon-b.png\"/> </a>");
            buffer.append(
                    "		<div style=\"-moz-margin-end: 0.6em; -moz-margin-start: 0; margin-bottom: 0; margin-top: 0;\">");
            buffer.append(
                    "			<h1 style=\"border-bottom: 2px solid threedlightshadow; font-size: 160%; margin: 0 0 0.2em;\">" + title + "</h1>");
            buffer.append("		</div>");
            buffer.append("	</div>");
            buffer.append("	<div id=\"feedContent\">");
            buffer.append("		<div class=\"entry\">");
            buffer.append("			<h2>");

            String date_string = new SimpleDateFormat("MM/dd").format(date);

            buffer.append("				<a href=\"https://www.ingod.today/?lang=zh-CN&amp;q=feed/")
                    .append(date_string).append("\">每日读经 ").append(date_string).append("</a>");
            buffer.append("			</h2>");
            buffer.append(
                    "			<div base=\"https://www.ingod.today/?lang=zh-CN&amp;q=feed\" class=\"feedEntryContent\">");

            Iterator<List<bible>> iterator = res.iterator();

            book book = new book();
            while (iterator.hasNext()) {
                List<bible> bs = iterator.next();

                Table table = book.findWith("WHERE book_id=? and language=?",
                        new Object[]{bs.get(0).getBookId(),
                                locale.equalsIgnoreCase(Locale.US.toString()) ? Locale.US.toString()
                                        : "zh_CN"});
                if (!table.isEmpty()) {
                    Row row = table.get(0);
                    book.setData(row);
                }

                buffer.append("<h3><a href=\"").append(this.getLink("bible")).append("/")
                        .append(book.getBookId()).append("\">").append(book.getBookName())
                        .append("</a></h3>");

                Iterator<bible> iter = bs.iterator();
                chapterId = 0;
                while (iter.hasNext()) {
                    bible bi = iter.next();

                    if (chapterId != bi.getChapterId()) {
                        buffer.append("<h4><a href=\"").append(this.getLink("bible")).append("/")
                                .append(book.getBookId()).append("/").append(bi.getChapterId()).append("\">")
                                .append(bi.getChapterId()).append("章</a></h4>");
                        chapterId = bi.getChapterId();
                    }

                    buffer.append(" ").append(bi.getPartId()).append(" ").append(bi.getContent());
                }
            }
            Element element = new Element();
            buffer.append("<br /><a href=\"").append(this.getLink("bible"))
                    .append("\" style=\"float:right\">")
                    .append(this.getProperty("subscribe.continue.caption")).append("</a>");
            buffer.append("			</div>");
            buffer.append("		</div>");
            buffer.append("		<div style=\"clear: both;\"></div>");
            buffer.append("	</div>");
            buffer.append("</div>");

            Element container = (Element) element.clone();
            container.setName("div");
            container.setAttribute("style", "background-color:#f5f5f5;text-align:justify");
            container.setData(buffer.toString());

            themail.setSubject("每日读经["
                    + format.format(new Date()) + "]");

            Element footer = new Element("div");

            subscription subscription = new subscription();
            Table table = subscription.findWith("WHERE available = 1", new Object[]{});
            for (Row fields : table) {
                subscription.setData(fields);

                footer.setAttribute("style",
                        "padding:10px;font-size:12px;color:#ccc;");
                footer.setData(
                        "让我们一起来养成每天读经的好习惯...<br />如果您不能正常访问此站点(<a href=\"https://www.ingod.today\">https://www.ingod.today</a>)，请尝试通过VPN进行访问，给您带来不便请谅解！如果你不想收到此邮件，请点击<a href=\"https://www.ingod.today/?q=services/unsubscribe/"
                                + subscription.getId() + "\">退订</a>。");

                themail.setBody(container + footer.toString());
                themail.setTo(subscription.getEmail());
                try {
                    themail.send();
                } catch (ApplicationException e) {
                    subscription.setAvailable(false);
                    subscription.update();
                }
            }

            Thread.sleep(1);
        } catch (ApplicationException e) {
            suggestion = new suggestion();
            suggestion.setEmail("services@ingod.today");
            suggestion.setIP("-");
            suggestion.setPostDate(new Date());
            suggestion.setStatus(false);
            suggestion.setTitle(e.getMessage() != null ? e
                    .getMessage() : "");

            note(e, suggestion);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            suggestion = new suggestion();
            suggestion.setEmail("services@ingod.today");
            suggestion.setIP("-");
            suggestion.setPostDate(new Date());
            suggestion.setStatus(false);
            suggestion.setTitle(e.getMessage() != null ? e
                    .getMessage() : "");

            note(e, suggestion);
        } finally {
            this.next.set(true);
        }
    }

    @Action("start")
    public void start() {
        TimeIterator iterator = new TimeIterator(00, 00, 00);
        iterator.setInterval(3600 * 24);
        this.getConfiguration().getOrDefault("default.base_url", "https://www.ingod.today/?q=");
        this.scheduler.schedule(new SchedulerTask() {
            private final Object o = new Object();

            @Override
            public void cancel() {
                // TODO Auto-generated method stub
                scheduler.cancel();
            }

            @Override
            public void start() {
                synchronized (o) {
                    loadBible();
                }
            }

            @Override
            public boolean next() {
                return next.get();
            }
        }, iterator);
    }

    public static void main(String[] args) throws ApplicationException {
        dailymail mailing = new dailymail();
        ApplicationManager.init();
        ApplicationManager.install(mailing);
        mailing.setLocale(Locale.US);
        mailing.loadBible();
    }

    public void note(Throwable e, suggestion suggestion) {
        // TODO Auto-generated method stub
        StackTraceElement[] trace = e.getStackTrace();

        StringBuilder errors = new StringBuilder("Details:\r\n");
        int i = 0;
        while (i < trace.length) {
            errors.append(trace[i++].toString()).append("\r\n");
        }

        if (e.getCause() != null) {
            StackTraceElement[] cause = e.getCause().getStackTrace();
            errors.append("\r\nCaused by:").append(e.getCause()).append("\r\n");
            i = 0;
            while (i < cause.length) {
                errors.append(cause[i++].toString()).append("\r\n");
            }
        }

        String content = errors.toString();
        suggestion.setContent(content);
        try {
            suggestion.append();
        } catch (ApplicationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void init() {
        // TODO Auto-generated constructor stub
        this.scheduler = new Scheduler(true);

        suggestion = new suggestion();
        suggestion.setEmail("services@ingod.today");
        suggestion.setIP("-");
        suggestion.setPostDate(new Date());
        suggestion.setStatus(false);

        EventDispatcher dispatcher = EventDispatcher.getInstance();
        dispatcher.registerHandler(DailyMailStartEvent.class, m -> {
            suggestion payload = m.getPayload();
            assert payload != null;
            payload.setTitle("Task has been started!");
            payload.setContent("Task has been started at " + new Date());
            try {
                payload.append();
            } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            start();
        });

        dispatcher.registerHandler(DailyMailEndEvent.class, m -> {
            suggestion payload = m.getPayload();
            assert payload != null;
            payload.setTitle("Task has been cancelled!");
            payload.setContent("Task has been cancelled at " + new Date());
            try {
                payload.append();
            } catch (ApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            scheduler.cancel();
        });

        dispatcher.dispatch(new DailyMailStartEvent(suggestion));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> dispatcher.dispatch(new DailyMailEndEvent(suggestion))));
    }

    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    private static class DailyMailStartEvent implements Event<suggestion> {
        private final suggestion suggestion;

        private DailyMailStartEvent(suggestion suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        public String getName() {
            return "Daily Mail Start";
        }

        @Override
        public suggestion getPayload() {
            return this.suggestion;
        }
    }

    private static class DailyMailEndEvent implements Event<suggestion> {
        private final suggestion suggestion;

        private DailyMailEndEvent(suggestion suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        public String getName() {
            return "Daily Mail End";
        }

        @Override
        public suggestion getPayload() {
            return this.suggestion;
        }
    }
}
