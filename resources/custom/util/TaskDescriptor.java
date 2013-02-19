package custom.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tinystruct.ApplicationException;

import custom.util.model.Book;
import custom.util.model.Chapter;
import custom.util.model.Section;


public class TaskDescriptor {
	private String value;
	private List<Book> books;

	enum Model {
		NONE,
		CHAPTER_CHAPTER,
		CHAPTER_CHAPTER_SECTION,
		CHAPTER_SECTION_SECTION,
		CHAPTER_SECTION_CHAPTER_SECTION, 
	}
	
	public TaskDescriptor()
	{
		this.books=new ArrayList<Book>();
	}
	
	public StringBuffer parse(String value) throws ApplicationException
	{
		this.value=value;
		
		StringBuffer buffer=new StringBuffer();
		if(this.value.indexOf(";")!=-1)
		{
			String[] books=this.value.split(";");
			for(int i=0;i<books.length;i++)
			{
				this.books.add(parseBook(books[i]));
			}
			
			Iterator<Book> iterator = this.books.iterator();
			while(iterator.hasNext())
			{
				if(buffer.length()==0)
				{
					buffer.append(iterator.next().toSQL());
				}
				else
				{
					buffer.append(" or ").append(iterator.next().toSQL());
				}
			}
		}
		else if(this.value.indexOf("^")!=-1)
		{
			buffer.append(parseBook(value).toSQL());
		}
			
		
		return buffer;
	}

	public Book parseBook(String value) throws ApplicationException
	{
		if(value.indexOf("^")==-1)
		{
			throw new ApplicationException("Invalid format value:"+value);
		}
		
		String[] bookInfo=value.split("\\^",2);
		
		String bookId=bookInfo[0];
		Book book=new Book(Integer.parseInt(bookId));
		
		List<Chapter> chapters=this.parseChapter(book,bookInfo[1]);
		Iterator<Chapter> iterator=chapters.iterator();
		while(iterator.hasNext())
		{
			book.newChapter(iterator.next());
		}
		return book;
	}
	
	private List<Chapter> parseChapter(Book book, String value) {
		// TODO Auto-generated method stub

		List<Chapter> chapters=new ArrayList<Chapter>();
		
		if(value.indexOf(',')!=-1)
		{
			String[] chapter_id=value.split(",");
			
			for(int i=0;i<chapter_id.length;i++)
			{
				Chapter chapter=new Chapter(Integer.parseInt(chapter_id[i]));
				chapters.add(chapter);
			}
		}
		else
		if(value.indexOf('-')!=-1)
		{
			String[] chapterInfo=value.split("-");
			
//			Model model=Model.NONE;
			if(chapterInfo[0].indexOf(':')==-1 && chapterInfo[1].indexOf(':')==-1)
			{
//				model=Model.CHAPTER_CHAPTER;
				
				book.fromNode(new Chapter(new Integer(chapterInfo[0])));
				book.toNode(new Chapter(new Integer(chapterInfo[1])));
			}
			else if(chapterInfo[0].indexOf(':')!=-1 && chapterInfo[1].indexOf(':')==-1)
			{
//				model=Model.CHAPTER_SECTION_SECTION;
				
				String[] info=chapterInfo[0].split(":");
				Chapter chapter=new Chapter(new Integer(info[0]));
				
				chapter.fromNode(new Section(new Integer(info[1])));
				chapter.toNode(new Section(new Integer(chapterInfo[1])));
				
				book.newChapter(chapter);
			}
			else if(chapterInfo[0].indexOf(':')==-1 && chapterInfo[1].indexOf(':')!=-1)
			{
//				model=Model.CHAPTER_CHAPTER_SECTION;
				
				book.fromNode(new Chapter(new Integer(chapterInfo[0])));
				
				String[] info=chapterInfo[1].split(":");
				
				Section section=new Section(new Integer(info[1]));
				Chapter chapter=new Chapter(new Integer(info[0]));
				chapter.newSection(section);
				
				book.toNode(chapter);
			}
			else if(chapterInfo[0].indexOf(':')!=-1 && chapterInfo[1].indexOf(':')!=-1)
			{
//				model=Model.CHAPTER_SECTION_CHAPTER_SECTION;
				
				String[] info=chapterInfo[0].split(":");
				Chapter chapter1=new Chapter(new Integer(info[0]));
				Section section1=new Section(new Integer(info[1]));
				chapter1.newSection(section1);
				
				String[] info1=chapterInfo[1].split(":");
				Chapter chapter2=new Chapter(new Integer(info1[0]));
				Section section2=new Section(new Integer(info1[1]));
				chapter2.newSection(section2);
				
				book.fromNode(chapter1);
				book.toNode(chapter2);
			}
			
		}
		else
		if(value.indexOf(':')!=-1)
		{
//			System.out.println(value);
			String[] chapterInfo=value.split(":");
			
			Chapter chapter=new Chapter(new Integer(chapterInfo[0]));
			Section section=new Section(new Integer(chapterInfo[1]));
			
			chapter.newSection(section);
			
			chapters.add(chapter);
		}
		else
		{
			Chapter chapter=new Chapter(new Integer(value));
			
			chapters.add(chapter);
		}
		
		return chapters;
	}
	
	public static void main(String[]args) throws ApplicationException
	{
//		String s="1^1";
//		String s="1^1-3";
//		String s="1^1,3";
//		String s="1^1:1";
//		String s="1^1:1-10";
//		String s="1^1:1-3:1";
		
		String s="19^58;41^8;12^18-19";
		
		TaskDescriptor task=new TaskDescriptor();
		System.out.println(task.parse(s));
	}
}
