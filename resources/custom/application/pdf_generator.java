package custom.application;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Field;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;
import com.itextpdf.text.pdf.draw.LineSeparator;

import custom.objects.bible;
import custom.objects.book;

public class pdf_generator {

	/**
	 * @param args
	 * @throws ApplicationException 
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, ApplicationException, IOException {
		// TODO Auto-generated method stub
		new pdf_generator().create("bible.pdf");
	}
	
	public void create(String fileName) throws DocumentException, ApplicationException, IOException
	{
	       // step 1
        Rectangle pagesize = new Rectangle(600f, 800f);
        
        pagesize.setBackgroundColor(new BaseColor(228,242,253));
        
        Document document = new Document(pagesize, 40f, 200f, 50f, 50f);
        // step 2
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        // step 3
        document.open();
        
        Image logo=PngImage.getImage("themes/images/pdf-logo.png");
        logo.scalePercent(85f);
        
        document.add(logo);
        
        book book=new book();
        Table table=book.findWith("WHERE language=? order by book_id", new Object[]{"zh_CN"});
        Iterator<Row> iterator=table.iterator();
        
        List list=new List(List.ORDERED);
        list.setIndentationLeft(36);
        list.setIndentationRight(36);

		BaseFont defaultFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",BaseFont.EMBEDDED);
		BaseFont headFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.EMBEDDED); 
		
		Font NormalStyle = new Font(defaultFont, 11, Font.NORMAL);
		Font HeaderStyle = new Font(headFont, 13, Font.BOLD);
		Font UnderlineHeaderStyle = new Font(headFont, 13, Font.UNDERLINE);
		Font BlueHeaderStyle = new Font(headFont, 18, Font.BOLD,BaseColor.BLUE);
		
        int i=1;
        while(iterator.hasNext())
        {
        	book.setData(iterator.next());
        	
            Phrase phrase = new Phrase();
            phrase.add(new Chunk(book.getBookName(),UnderlineHeaderStyle).setAction(PdfAction.gotoLocalPage(String.valueOf(i++), false)));
            ListItem item=new ListItem();
            item.add(phrase);
            list.add(item);
        }
        // step 4
        
        Paragraph bookName=new Paragraph(new Chunk("中文圣经（简体版）",BlueHeaderStyle));
        document.add(bookName);
        
        document.add(list);
        
        bible bible=new bible();
        bible.setTableName("zh_CN");
        
        Iterator<Row> page_iterator=table.iterator();
        i=1;
        while(page_iterator.hasNext())
        {
        	book.setData(page_iterator.next());

            document.add(logo);
            document.add(Chunk.NEWLINE);
            
        	Paragraph graph = new Paragraph(new Chunk(book.getBookName(),HeaderStyle).setLocalDestination(String.valueOf(i)));
            
            Chapter chapter=new Chapter(book.getBookName(),i);
            chapter.setTitle(graph);
            chapter.setBookmarkOpen(false);
            
            bible m=new bible();
            m.setTableName("zh_CN");
            int max_chapter=bible.setRequestFields("max(chapter_id) as max_chapter").findWith("WHERE book_id=?",new Object[]{book.getBookId()}).get(0).get(0).get("max_chapter").intValue();
            for(int k=0;k<max_chapter;k++)
            {
            	Paragraph title = new Paragraph();
	            title.add(new Chunk("第"+(k+1)+"章",NormalStyle));
	        	LineSeparator line = new LineSeparator();
	        	line.setLineColor(BaseColor.GRAY);
	            document.add(line);
	            Section section=chapter.addSection(title);
	            section.setBookmarkOpen(false);
	            section.setTitle(title);
	            
	            String where = "WHERE book_id="+book.getBookId()+" and chapter_id="+(k+1)+" order by part_id";
	            
	            Table vtable=m.findWith(where,new Object[]{});
	            
	            Paragraph partNode=new Paragraph();
	            partNode.setFirstLineIndent(10.0f);
	            Field fields;
	    		for(Enumeration<Row> ptable=vtable.elements();ptable.hasMoreElements();)
	            {
	    			Row row=ptable.nextElement();
	    			Iterator<Field> piterator=row.iterator();
	    			
	    			while(piterator.hasNext())
	    			{
	    				fields=piterator.next();
	    				String finded=fields.get("content").value().toString();
	    				
	    				partNode.add(new Chunk(fields.get("part_id").value().toString(),new Font(FontFamily.COURIER,9,4,BaseColor.RED)));
	    				partNode.add(new Chunk(finded,NormalStyle));
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

}
