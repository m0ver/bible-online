package custom.application;

import custom.objects.JPV;
import custom.objects.KJV;
import custom.objects.bible;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.system.ApplicationManager;
import org.tinystruct.system.util.URLResourceLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class crawler extends AbstractApplication {

    @Override
    public void init() {
        // TODO Auto-generated method stub
        this.setAction("start-crawler", "start");
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    public void startJPV() throws MalformedURLException {
        try {
            JPV jpv = new JPV();

            bible bible = new bible();
            bible.setTableName("zh_CN");
            int partId;
            String text;
            URL url;
            URLResourceLoader loader;
            Row o;
            StringBuffer content;
            String words;
            Pattern pat = Pattern.compile("</span>\\s*(.*)(\r\n)*");
            Matcher mat;
            Table list;
            for (int i = 1; i <= 66; i++) {
                o = bible.findOne("SELECT max(chapter_id) as n FROM zh_CN WHERE book_id=?", new Object[]{i});
                int n = o.get(0).get("n").intValue();
                for (int j = 1; j <= n; j++) {
                    jpv.setBookId(i);
                    jpv.setChapterId(j);

                    list = jpv.find("SELECT * FROM KJV WHERE book_id = ? and chapter_id = ? order by id desc", new Object[]{jpv.getBookId(), jpv.getChapterId()});
                    if (list.size() > 0) {
                        continue;
                    }

                    partId = 1;

                    url = new URL("http://www.wordplanet.org/kj/" + (i < 10 ? "0" + i : i) + "/" + j + ".htm");
                    loader = new URLResourceLoader(url);
                    content = loader.getContent();
                    String defaults = "<span class=\"verse\" id=\"1\">1 </span>";
                    if (content.indexOf(defaults) == -1) defaults = "<span class=\"verse\" id=\"1\">1</span>";
                    words = content.substring(content.indexOf(defaults), content.indexOf("<!--... sharper than any twoedged sword... -->"));
                    words = words.replaceAll("<br/>", "\r\n");
                    mat = pat.matcher(words);

                    while (mat.find()) {
                        if (list.size() > 0 && list.firstElement().getFieldInfo("part_id").intValue() >= partId) {
                            continue;
                        }
                        jpv.setPartId(partId++);
                        text = mat.group().replaceAll("</span>\\s*", "");
                        jpv.setContent(text);
                        jpv.append();
                        System.out.println(text);
                    }
                }
            }
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void start() throws MalformedURLException {
        try {

            bible bible = new bible();
            bible.setTableName("zh_CN");
            int partId;
            String text;
            URL url;
            URLResourceLoader loader;
            Row o;
            StringBuffer content;
            String words;
            Pattern pat = Pattern.compile("<span class=\"verse\" id=\"(\\d+)\">.*</span>\\s*(.*)(\r\n)*");

            Matcher mat;
            Table list = null;

            for (int i = 1; i <= 66; i++) {
                o = bible.findOne("SELECT max(chapter_id) as n FROM zh_CN WHERE book_id=?", new Object[]{i});
                int n = o.get(0).get("n").intValue();
                url = new URL("https://www.wordplanet.org/kj/b" + (i < 10 ? "0" + i : i) + ".htm");

                loader = new URLResourceLoader(url, true);
                content = loader.getContent();

                int j = 1;
                KJV KJV = new KJV();
                KJV.setBookId(i);

                String defaults = "<h3>Chapter 1</h3>";
                words = content.substring(content.indexOf(defaults) + defaults.length(), content.lastIndexOf("<div class=\"alignRight ym-noprint\">"));
                words = words.replaceAll("<br/>", "\r\n");
                mat = pat.matcher(words);

                while (mat.find()) {
                    partId = Integer.parseInt(mat.group(1));
                    if (partId == 1) {
                        KJV.setChapterId(j);

                        // Check the chapter id if it's existing in the db.
                        list = KJV.find("SELECT * FROM KJV WHERE book_id = ? and chapter_id = ? order by part_id desc", new Object[]{KJV.getBookId(), KJV.getChapterId()});
                        j++;
                    }

                    if (null != list && !list.isEmpty() && list.firstElement().getFieldInfo("part_id").intValue() >= partId) {
                        continue;
                    }

                    KJV kjv = new KJV();
                    kjv.setBookId(i);
                    kjv.setChapterId(KJV.getChapterId());
                    text = mat.group().replaceAll("<span class=\"verse\" id=\"" + partId + "\">" + partId + "</span>\\s*", "");
                    kjv.setContent(text);
                    kjv.setPartId(partId++);
                    kjv.append();
                    System.out.println(text);
                }
            }
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws ApplicationException
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws ApplicationException, IOException {
        // TODO Auto-generated method stub
//    ApplicationManager.init();
        ApplicationManager.install(new crawler());
        ApplicationManager.call("start-crawler", null);


//    URL url = new URL("https://www.wordplanet.org/kj/b01.htm");
//    URLConnection urlConnection = url.openConnection();
//    urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:80.0) Gecko/20100101 Firefox/80.0");
//    InputStream inputStream = urlConnection.getInputStream();
//
//    InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
//    BufferedReader bufferedReader = new BufferedReader(reader);
//
//    StringBuffer context = new StringBuffer();
//    for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
//      context.append(line).append("\r\n");
//    }
//
//    bufferedReader.close();
//    reader.close();
//    inputStream.close();
//    System.out.println("context = " + context);
    /*
    Pattern pat = Pattern.compile("</span>\\s*(.*)(\r\n)*");  

    URL url = new URL("http://www.wordplanet.org/jp/19/1.htm");
    URLFileLoader loader = new URLFileLoader(url);
    StringBuffer content = loader.getContent();
    String defaults = "<p><span class=\"verse\" id=\"1\">1 </span>";
    if(content.indexOf(defaults) == -1) defaults="<p><span class=\"verse\" id=\"1\">1</span>";
    String words = content.substring(content.indexOf(defaults), content.indexOf("<!--... sharper than any twoedged sword... -->"));
    words = words.replaceAll("<br/>", "\r\n");
    Matcher mat = pat.matcher(words);
    while(mat.find()) {
      System.out.println(mat.group());
    }
    System.out.println(words);
    System.exit(0);
*/
    }

}
