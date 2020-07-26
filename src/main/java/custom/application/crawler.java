package custom.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.FieldInfo;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.system.ApplicationManager;
import org.tinystruct.system.util.URLResourceLoader;

import custom.objects.JPV;
import custom.objects.KRV;
import custom.objects.bible;
import custom.objects.book;

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
      
      bible bible= new bible();
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
      for(int i=1;i<=66;i++) {
        o = bible.findOne("SELECT max(chapter_id) as n FROM zh_CN WHERE book_id=?", new Object[]{i});
        int n = o.get(0).get("n").intValue();
        for(int j=1;j<=n;j++) {
          jpv.setBookId(i);
          jpv.setChapterId(j);
          
          list = jpv.find("SELECT * FROM JPV WHERE book_id = ? and chapter_id = ? order by id desc", new Object[]{jpv.getBookId(),jpv.getChapterId()});
          if(list.size() > 0) {
            continue;
          }
          
          partId = 1;
          
          url = new URL("http://www.wordplanet.org/jp/"+(i<10?"0"+i:i)+"/"+j+".htm");
          loader = new URLResourceLoader(url);
          content = loader.getContent();
          String defaults = "<span class=\"verse\" id=\"1\">1 </span>";
          if(content.indexOf(defaults) == -1) defaults="<span class=\"verse\" id=\"1\">1</span>";
          words = content.substring(content.indexOf(defaults), content.indexOf("<!--... sharper than any twoedged sword... -->"));
          words = words.replaceAll("<br/>", "\r\n");
          mat = pat.matcher(words);

          while(mat.find()) {
            if(list.size() > 0 && list.firstElement().getFieldInfo("part_id").intValue() >= partId) {
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
      KRV krv = new KRV();
      
      bible bible= new bible();
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
      for(int i=1;i<=66;i++) {
        o = bible.findOne("SELECT max(chapter_id) as n FROM zh_CN WHERE book_id=?", new Object[]{i});
        int n = o.get(0).get("n").intValue();
        for(int j=1;j<=n;j++) {
          krv.setBookId(i);
          krv.setChapterId(j);
          
          list = krv.find("SELECT * FROM KRV WHERE book_id = ? and chapter_id = ? order by id desc", new Object[]{krv.getBookId(),krv.getChapterId()});
          if(list.size() > 0) {
            continue;
          }
          
          partId = 1;
          
          url = new URL("http://www.wordplanet.org/kr/"+(i<10?"0"+i:i)+"/"+j+".htm");
          loader = new URLResourceLoader(url, true);
          content = loader.getContent();
          String defaults = "<!--... the Word of God:-->";
          words = content.substring(content.indexOf(defaults)+defaults.length(), content.indexOf("<!--... sharper than any twoedged sword... -->"));
          words = words.replaceAll("<br/>", "\r\n");
          mat = pat.matcher(words);

          while(mat.find()) {
            if(list.size() > 0 && list.firstElement().getFieldInfo("part_id").intValue() >= partId) {
              continue;
            }
            krv.setPartId(partId++);
            text = mat.group().replaceAll("</span>\\s*", "");
            krv.setContent(text);
            krv.append();
            System.out.println(text);
          }
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
  public static void main(String[] args) throws ApplicationException, MalformedURLException {
    // TODO Auto-generated method stub
//    ApplicationManager.init();
    ApplicationManager.install(new crawler());
    ApplicationManager.call("start-crawler", null);
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
*/  }

}
