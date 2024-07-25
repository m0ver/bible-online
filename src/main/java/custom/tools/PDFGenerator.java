package custom.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.draw.LineSeparator;
import custom.objects.bible;
import custom.objects.book;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Field;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.system.annotation.Action;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PDFGenerator extends AbstractApplication {

    @Action("New-International-Version.pdf")
    public void createNIV() throws ApplicationException, DocumentException, IOException {
        create("New-International-Version.pdf", "NIV");
    }

    @Action("English-Standard-Version.pdf")
    public void createESV() throws ApplicationException, DocumentException, IOException {
        create("English-Standard-Version.pdf", "ESV");
    }

    @Action("CUV.pdf")
    public void createCUV() throws ApplicationException, DocumentException, IOException {
        create("CUV.pdf", "zh_CN");
    }

    @Action("CUV-Traditional.pdf")
    public void createCUVTraditional() throws ApplicationException, DocumentException, IOException {
        create("CUV-Traditional.pdf", "zh_TW");
    }

    public void create(String fileName, String tableName) throws DocumentException, ApplicationException, IOException {
        // step 1
        Rectangle pagesize = new Rectangle(280f, 420f);

        pagesize.setBackgroundColor(new BaseColor(228, 242, 253));

        Document document = new Document(pagesize, 30f, 30f, 37f, 37f);
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        writer.setBoxSize("default", pagesize);

        FontFactory.registerDirectories();

        // Load font using ClassLoader
        // Obtain the URL of the font file from the resources folder
        URL url = getClass().getClassLoader().getResource("fonts/STSongStd-Light.ttf");

        // Convert URL to font file path
        assert url != null;
        String fontPath = url.getPath();

        // Replace special characters in the path (if any)
        fontPath = URLDecoder.decode(fontPath, StandardCharsets.UTF_8);

        // Register font with iTextPDF
        BaseFont defaultFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

        Font NormalStyle = FontFactory.getFont("Times New Roman", 12, Font.NORMAL);
        Font HeaderStyle = FontFactory.getFont("Times New Roman", 14, Font.BOLD);
//		Font UnderlineHeaderStyle =  FontFactory.getFont("Times New Roman",15, Font.UNDERLINE);
        Font BlueHeaderStyle = FontFactory.getFont("Times New Roman", 20, Font.BOLD, BaseColor.BLUE);

        String lang = "en_US";
        String version = "Holy Bible (New International Version)";
        if (tableName.equalsIgnoreCase("ESV")) {
            version = "Holy Bible (English Standard Version)";
        }

        if (tableName.equalsIgnoreCase("zh_CN")) {
            NormalStyle = new Font(defaultFont, 11, Font.NORMAL);
            HeaderStyle = new Font(defaultFont, 13, Font.BOLD);
//			UnderlineHeaderStyle = new Font(headFont, 13, Font.UNDERLINE);
            BlueHeaderStyle = new Font(defaultFont, 18, Font.BOLD, BaseColor.BLUE);

            lang = "zh_CN";
            version = "中文圣经（简体版）";
        }
        /**
         * HeaderFooter的第2个参数为非false时代表打印页码 
         * 页眉页脚中也可以加入图片，并非只能是文字 
         */
        PDFPageFooter header = new PDFPageFooter();
        writer.setPageEvent(header);

        // step 3
        document.open();

        Image logo = PngImage.getImage("themes/images/pdf-logo.png");
        logo.scalePercent(36.8f);

        document.add(logo);

        book book = new book();
        Table table = book.findWith("WHERE language=? order by book_id", new Object[]{lang});
        Iterator<Row> iterator = table.iterator();

        List list = new List(List.ORDERED);
        //list.setIndentationLeft(36);
        //list.setIndentationRight(36);

        int i = 1;
        while (iterator.hasNext()) {
            book.setData(iterator.next());

            Phrase phrase = new Phrase();
            phrase.add(new Chunk(book.getBookName(), NormalStyle).setAction(PdfAction.gotoLocalPage(String.valueOf(i++), false)));
            ListItem item = new ListItem();
            item.add(phrase);
            list.add(item);
        }
        // step 4

        Paragraph bookName = new Paragraph(new Chunk(version, BlueHeaderStyle));

        document.add(bookName);

        document.add(list);

        bible bible = new bible();
        bible.setTableName(tableName);

        Iterator<Row> page_iterator = table.iterator();
        i = 1;
        while (page_iterator.hasNext()) {
            book.setData(page_iterator.next());

            document.add(logo);
            document.add(Chunk.NEWLINE);

            Paragraph graph = new Paragraph(new Chunk(book.getBookName(), HeaderStyle).setLocalDestination(String.valueOf(i)));

            Chapter chapter = new Chapter(book.getBookName(), i);
            chapter.setTitle(graph);
            chapter.setBookmarkOpen(false);

            int max_chapter = bible.setRequestFields("max(chapter_id) as max_chapter").findWith("WHERE book_id=?", new Object[]{book.getBookId()}).get(0).get(0).get("max_chapter").intValue();
            for (int k = 0; k < max_chapter; k++) {
                Paragraph title = new Paragraph();

                if (tableName.equalsIgnoreCase("zh_CN"))
                    title.add(new Chunk("第" + (k + 1) + "章", NormalStyle));
                else
                    title.add(new Chunk("Chapter " + (k + 1), NormalStyle));

                LineSeparator line = new LineSeparator();
                line.setLineColor(BaseColor.GRAY);
                document.add(line);
                Section section = chapter.addSection(title);
                section.setBookmarkOpen(false);
                section.setTitle(title);

                String where = "WHERE book_id=" + book.getBookId() + " and chapter_id=" + (k + 1) + " order by part_id";

                Table vtable = bible.setRequestFields("*").findWith(where, new Object[]{});

                Paragraph partNode = new Paragraph();
                partNode.setFirstLineIndent(10.0f);
                Field fields;
                for (Row row : vtable) {
                    for (Field field : row) {
                        fields = field;
                        String finded = fields.get("content").value().toString();

                        partNode.add(new Chunk(fields.get("part_id").value().toString(), new Font(FontFamily.COURIER, 9, 4, BaseColor.RED)));
                        partNode.add(new Chunk(finded, NormalStyle));
                    }
                }

                section.add(partNode);
                section.add(Chunk.NEWLINE);
            }

            document.add(chapter);


            i++;
        }

        document.close();

    }


    /**
     * Initialize for an application once it's loaded.
     */
    @Override
    public void init() {
        this.setTemplateRequired(false);
    }

    /**
     * Return the version of the application.
     *
     * @return version
     */
    @Override
    public String version() {
        return null;
    }
}
