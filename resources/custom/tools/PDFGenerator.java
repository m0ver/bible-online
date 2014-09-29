package custom.tools;

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
import com.itextpdf.text.FontFactory;
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

public class PDFGenerator {

	/**
	 * @param args
	 * @throws ApplicationException 
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, ApplicationException, IOException {
		// TODO Auto-generated method stub
		new PDFGenerator().create("New-International-Version.pdf","NIV");
		new PDFGenerator().create("English-Standard-Version.pdf","ESV");
		new PDFGenerator().create("bible.pdf","zh_CN");
	}
	
	public void create(String fileName, String tableName) throws DocumentException, ApplicationException, IOException
	{
	       // step 1
        Rectangle pagesize = new Rectangle(360f, 480f);
        
        pagesize.setBackgroundColor(new BaseColor(228,242,253));
        
        Document document = new Document(pagesize, 30f, 30f, 37f, 37f);
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        writer.setBoxSize("default", pagesize);
        
        FontFactory.registerDirectories();

		BaseFont defaultFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",BaseFont.EMBEDDED);
		BaseFont headFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.EMBEDDED); 
        
		Font NormalStyle = FontFactory.getFont("Times New Roman",12, Font.NORMAL);
		Font HeaderStyle = FontFactory.getFont("Times New Roman",14, Font.BOLD);
//		Font UnderlineHeaderStyle =  FontFactory.getFont("Times New Roman",15, Font.UNDERLINE);
		Font BlueHeaderStyle = FontFactory.getFont("Times New Roman", 20, Font.BOLD,BaseColor.BLUE);

		String lang = "en_US";
		String version = "Holy Bible (New International Version)";
		if(tableName.equalsIgnoreCase("ESV")) {
			version = "Holy Bible (English Standard Version)";
		}
		
		if(tableName.equalsIgnoreCase("zh_CN")) {
			NormalStyle = new Font(defaultFont, 11, Font.NORMAL);
			HeaderStyle = new Font(headFont, 13, Font.BOLD);
//			UnderlineHeaderStyle = new Font(headFont, 13, Font.UNDERLINE);
			BlueHeaderStyle = new Font(headFont, 18, Font.BOLD,BaseColor.BLUE);
			
			lang="zh_CN";
			version="中文圣经（简体版）";
		}
        /** 
         * HeaderFooter的第2个参数为非false时代表打印页码 
         * 页眉页脚中也可以加入图片，并非只能是文字 
         */  
        PDFPageFooter header=new PDFPageFooter();  
        writer.setPageEvent(header);
        
        // step 3
        document.open();
        
        Image logo=PngImage.getImage("themes/images/pdf-logo.png");
        
        logo.scalePercent(50f);
         
        document.add(logo);
        
        book book=new book();
        Table table=book.findWith("WHERE language=? order by book_id", new Object[]{lang});
        Iterator<Row> iterator=table.iterator();
        
        List list=new List(List.ORDERED);
        //list.setIndentationLeft(36);
        //list.setIndentationRight(36);
		
        int i=1;
        while(iterator.hasNext())
        {
        	book.setData(iterator.next());
        	
            Phrase phrase = new Phrase();
            phrase.add(new Chunk(book.getBookName(),NormalStyle).setAction(PdfAction.gotoLocalPage(String.valueOf(i++), false)));
            ListItem item=new ListItem();
            item.add(phrase);
            list.add(item);
        }
        // step 4
        
        Paragraph bookName=new Paragraph(new Chunk(version,BlueHeaderStyle));

        document.add(bookName);
        
        document.add(list);
        
        bible bible=new bible();
        bible.setTableName(tableName);
        
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
            m.setTableName(tableName);
            
            int max_chapter=bible.setRequestFields("max(chapter_id) as max_chapter").findWith("WHERE book_id=?",new Object[]{book.getBookId()}).get(0).get(0).get("max_chapter").intValue();
            for(int k=0;k<max_chapter;k++)
            {
            	Paragraph title = new Paragraph();
            	
            	if(tableName.equalsIgnoreCase("zh_CN"))
	            title.add(new Chunk("第"+(k+1)+"章",NormalStyle));
            	else
	            title.add(new Chunk("Chapter "+(k+1),NormalStyle));

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
