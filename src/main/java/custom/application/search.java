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
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.*;
import org.tinystruct.http.Reforward;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.Session;
import org.tinystruct.net.URLHandler;
import org.tinystruct.net.URLHandlerFactory;
import org.tinystruct.net.URLRequest;
import org.tinystruct.net.URLResponse;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.util.StringUtilities;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.tinystruct.http.Constants.HTTP_REQUEST;
import static org.tinystruct.http.Constants.HTTP_RESPONSE;

public class search extends AbstractApplication {
    protected static final String MODEL = "deepseek/deepseek-r1:free";
    private Request request;
    private Response response;
    private User usr;
    private static final Logger logger = Logger.getLogger(search.class.getName());

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
        this.setText("ask.submit.caption");
        this.setText("search.strict.mode");

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
        this.setText("search.info", 0, 0, 0);

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
        this.setVariable("search.title", "");
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
        initialize(session);

        if (this.request.getParameter("keyword") != null)
            return this.query(this.request.getParameter("keyword"));

        return this;
    }

    @Action("bible/search")
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
        this.setVariable("base_url", String.valueOf(getContext().getAttribute("HTTP_HOST")));

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
                + query + "&lang=" + lang + "&page";
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

        this.setText("search.info", pager.getSize(), start, end);

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
    public Object advanced(Request request) throws ApplicationException {
        this.setVariable("base_url", String.valueOf(getContext().getAttribute("HTTP_HOST")));
        this.request = (Request) getContext().getAttribute(HTTP_REQUEST);

        Session session = request.getSession(); //@TODO
        initialize(session);

        String query;
        if (request.getParameter("keyword") != null && !request.getParameter("keyword").isEmpty()) {
            query = request.getParameter("keyword");
        } else {
            return this;
        }

        query = StringUtilities.htmlSpecialChars(query);

        int page = 1, pageSize = 10, total = 0;
        this.request = (Request) getContext().getAttribute(HTTP_REQUEST);

        if (this.request.getParameter("page") == null || this.request.getParameter("page").trim().isEmpty()) {
            page = 1;
        } else {
            page = Integer.parseInt(this.request.getParameter("page").toString());
        }

        // Initialize search components
        StringBuilder html = new StringBuilder();
        long startTime = System.currentTimeMillis();

        try {
            // Use OpenAI to find relevant verses
            List<SearchResult> searchResults = findRelevantVerses(query);

            // Sort by relevance score
            Collections.sort(searchResults, Comparator.comparingDouble(SearchResult::getRelevance).reversed());

            // Calculate pagination
            int startIndex = (page - 1) * pageSize;
            total = searchResults.size();
            int endIndex = Math.min(startIndex + pageSize, total);

            List<SearchResult> pageResults = searchResults.subList(startIndex, endIndex);

            // Setup pagination
            Pager pager = new Pager();
            pager.setPageSize(pageSize);
            pager.setCurrentPage(page);
            pager.setListSize(total);

            // Build results HTML
            html.append("<ol class=\"searchresults\" start=\"").append(startIndex + 1).append("\">\r\n");

            for (SearchResult result : pageResults) {
                String content = result.getContent();
                String bookName = result.getBookName();
                String chapterId = result.getChapterId();
                String partId = result.getPartId();
                String bookId = result.getBookId();
                double relevance = result.getRelevance();
                String explanation = result.getExplanation();

                // Highlight matching terms
                content = highlightMatchingTerms(content, query);

                html.append("<li class=\"").append(startIndex % 2 == 0 ? "even" : "odd").append("\">")
                        .append("<a href=\"").append(getContext().getAttribute("HTTP_HOST"))
                        .append("bible/").append(bookId).append("/")
                        .append(chapterId).append("/").append(partId)
                        .append("\" target=\"_blank\">")
                        .append(this.setText("search.bible.info", bookName, chapterId, partId))
                        .append("</a>")
                        .append("<p>").append(content).append("</p>")
                        .append("<div class=\"relevance\">")
                        .append("Relevance Score: ").append(String.format("%.2f", relevance))
                        .append("</div>")
                        .append("<div class=\"explanation\">").append(explanation).append("</div>")
                        .append("</li>\r\n");

                startIndex++;
            }

            html.append("</ol>\r\n");

            // Add pagination
            String actionURL = getContext().getAttribute("HTTP_HOST") + "bible/advsearch/" + query + "&page";
            pager.setFirstPageText(this.getProperty("page.first.text"));
            pager.setLastPageText(this.getProperty("page.last.text"));
            pager.setCurrentPageText(this.getProperty("page.current.text"));
            pager.setNextPageText(this.getProperty("page.next.text"));
            pager.setEndPageText(this.getProperty("page.end.text"));
            pager.setControlBarText(this.getProperty("page.controlbar.text"));

            html.append("<div class=\"pagination\" style=\"cursor:default\">")
                    .append(pager.getPageControlBar(actionURL))
                    .append("</div>\r\n");
            html.append("<!-- Search completed in ").append(System.currentTimeMillis() - startTime).append("ms -->");

            // Set response variables
            int start = (page - 1) * pageSize + 1;
            int end = Math.min(page * pageSize, total);

            this.setVariable("start", String.valueOf(start));
            this.setVariable("end", String.valueOf(end));
            this.setVariable("size", String.valueOf(total));
            this.setVariable("value", html.toString());
            this.setVariable("keyword", query);
            this.setVariable("search.title", query + " - ");
            this.setVariable("action", getContext().getAttribute("HTTP_HOST") + getContext().getAttribute("REQUEST_PATH").toString());

            this.setText("search.info", total, start, end);

        } catch (Exception e) {
            throw new ApplicationException("Error performing advanced search: " + e.getMessage(), e);
        }

        return this;
    }

    private void initialize(Session session) {
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
    }

    private List<SearchResult> findRelevantVerses(String query) {
        List<SearchResult> results = new ArrayList<>();

        try {
            // Get the current locale
            Locale locale = this.getLocale();

            // First analyze the query using OpenAI with language context
            Builder analysisResult = getOpenAIAnalysis(query, locale);

            if (analysisResult == null) return results;

            // Extract verse references from analysis
            Builders verseRefs = (Builders) analysisResult.get("verses");
            if (verseRefs != null && !verseRefs.isEmpty()) {
                // Build SQL condition for the referenced verses
                StringBuilder condition = new StringBuilder();
                List<Object> params = new ArrayList<>();

                for (Builder ref : verseRefs) {
                    if (condition.length() > 0) {
                        condition.append(" OR ");
                    }
                    condition.append("(book.book_name = ? AND bible.chapter_id = ? AND bible.part_id = ?)");
                    params.add(ref.get("book"));
                    params.add(ref.get("chapter"));
                    params.add(ref.get("verse"));
                }

                bible bible = new bible();
                book book = new book();

                // Set the Bible version based on locale
                if (locale.toString().equalsIgnoreCase(Locale.US.toString())) {
                    bible.setTableName("NIV");
                } else if (locale.toString().equalsIgnoreCase(Locale.UK.toString())) {
                    bible.setTableName("ESV");
                } else {
                    bible.setTableName(locale.toString());
                }

                // Get only the referenced verses from database
                String bibleSQL = "SELECT bible.book_id, bible.chapter_id, bible.part_id, bible.content, " +
                        "book.book_name " +
                        "FROM " + bible.getTableName() + " as bible " +
                        "LEFT JOIN " + book.getTableName() + " as book ON bible.book_id = book.book_id " +
                        "WHERE (" + condition + ") AND book.language = ?";
                params.add(locale.toString());

                Table relevantVerses;
                relevantVerses = bible.find(bibleSQL, params.toArray());

                // Convert to SearchResult objects
                for (Row row : relevantVerses) {
                    String content = row.getFieldInfo("content").value().toString();
                    String bookName = row.getFieldInfo("book_name").value().toString();
                    String chapterId = row.getFieldInfo("chapter_id").value().toString();
                    String partId = row.getFieldInfo("part_id").value().toString();
                    String bookId = row.getFieldInfo("book_id").value().toString();

                    // Find matching explanation from OpenAI analysis
                    String explanation = "";
                    double relevance = 0;
                    for (Map<String, Object> ref : verseRefs) {
                        if (ref.get("book").equals(bookName) &&
                                ref.get("chapter").toString().equals(chapterId) &&
                                ref.get("verse").toString().equals(partId)) {
                            relevance = Double.parseDouble(ref.get("relevance").toString());
                            explanation = (String) ref.get("explanation");
                            break;
                        }
                    }

                    results.add(new SearchResult(
                            bookId, bookName, chapterId, partId,
                            content, relevance, explanation
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private Builder getOpenAIAnalysis(String query, Locale locale) throws Exception {
        // Prepare the request body
        Builder requestBuilder = new Builder();
        requestBuilder.put("model", MODEL);

        // Create prompt with language context
        String promptTemplate = "Given this Bible search query in %s: '%s'\n" +
                "Find the most relevant Bible verses and return them in this JSON format:\n" +
                "{\n" +
                "  \"verses\": [\n" +
                "    {\n" +
                "      \"book\": \"BookName\",\n" +
                "      \"chapter\": ChapterNumber,\n" +
                "      \"verse\": VerseNumber,\n" +
                "      \"relevance\": RelevanceScore,\n" +
                "      \"explanation\": \"Why this verse is relevant\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Include up to 5 most relevant verses, but each item only one verse. Relevance score should be between 0 and 1.";

        String prompt = String.format(promptTemplate, locale.getDisplayLanguage(), query);
        requestBuilder.put("prompt", prompt);
        requestBuilder.put("max_tokens", 3000);
        requestBuilder.put("temperature", 0.3);

        // Make the API request
        String api = "/v1/completions";
        String url = getConfiguration().get("openai.api_endpoint") + api;
        String jsonResponse = makeOpenAIRequest(url, requestBuilder.toString());
        jsonResponse = processAIResponse(jsonResponse);
        jsonResponse = jsonResponse.replaceAll("\\\\n", "\n");
        jsonResponse = jsonResponse.replaceAll("\\\\", "");
        jsonResponse = jsonResponse.replaceAll("撒母耳记上", "撒母耳记（上）");
        jsonResponse = jsonResponse.replaceAll("撒母耳记下", "撒母耳记（下）");
        jsonResponse = jsonResponse.replaceAll("列王记上", "列王记（上）");
        jsonResponse = jsonResponse.replaceAll("列王记上", "列王记（上）");
        jsonResponse = jsonResponse.replaceAll("历代志上", "历代志（上）");
        jsonResponse = jsonResponse.replaceAll("历代志下", "历代志（下）");
        try {
            // Parse the JSON response
            Builder resultBuilder = new Builder();
            resultBuilder.parse(jsonResponse);

            return resultBuilder;
        } catch (ApplicationException e) {
            logger.severe(e.getMessage() + ":\n" + jsonResponse);
        }

        return null;
    }

    private String makeOpenAIRequest(String urlString, String requestBody) throws Exception {
        URL url = new URL(urlString);
        URLRequest request = new URLRequest(url);

        String OPENAI_API_KEY = getConfiguration().get("openai.api_key");

        // Set headers
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
        request.setHeader("Referer", "Test");
        request.setHeader("User-Agent", "Test");

        // Set request method and body
        request.setMethod("POST");
        request.setBody(requestBody);

        URLHandler handler = URLHandlerFactory.getHandler(url);
        // Make the request
        URLResponse response = handler.handleRequest(request);

        if (response.getStatusCode() != 200) {
            throw new ApplicationException(response.getBody());
        }

        // Parse JSON response using Builder
        Builder responseBuilder = new Builder();
        responseBuilder.parse(response.getBody());

        // Extract text content from choices array
        Builders choices = (Builders) responseBuilder.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Builder firstChoice = choices.get(0);
            return firstChoice.get("text").toString().trim();
        }

        throw new ApplicationException("No text content found in OpenAI response");
    }

    private String processAIResponse(String response) throws ApplicationException {
        // Regex to detect PlantUML code block - either in markdown format or raw format
        Pattern pattern = Pattern.compile("```json(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String result = matcher.group(0);
            return result.replaceAll("```json", "").replaceAll("```", "");
        }

        pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return "{}";
    }

    // Helper class to store search results
    private static class SearchResult {
        private final String bookId;
        private final String bookName;
        private final String chapterId;
        private final String partId;
        private final String content;
        private final double relevance;
        private final String explanation;

        public SearchResult(String bookId, String bookName, String chapterId, String partId,
                            String content, double relevance, String explanation) {
            this.bookId = bookId;
            this.bookName = bookName;
            this.chapterId = chapterId;
            this.partId = partId;
            this.content = content;
            this.relevance = relevance;
            this.explanation = explanation;
        }

        public String getBookId() {
            return bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public String getChapterId() {
            return chapterId;
        }

        public String getPartId() {
            return partId;
        }

        public String getContent() {
            return content;
        }

        public double getRelevance() {
            return relevance;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    private String highlightMatchingTerms(String content, String query) {
        // Split query into words
        String[] queryWords = query.toLowerCase().split("\\s+");

        // Highlight each matching word
        for (String word : queryWords) {
            if (word.length() > 2) { // Only highlight words longer than 2 characters
                content = content.replaceAll(
                        "(?i)(" + Pattern.quote(word) + ")",
                        "<span class=\"highlight\">$1</span>"
                );
            }
        }

        return content;
    }

}
