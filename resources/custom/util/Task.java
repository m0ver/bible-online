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
package custom.util;


public class Task {
	public String task;
	public StringBuffer buffer;
	public Task()
	{
		this.buffer=new StringBuffer();
	}
	
	public StringBuffer getBooks(String booksInfo)
	{
		StringBuffer buffer=new StringBuffer();
		String[] books=booksInfo.split("~");
		for(int i=0;i<books.length;i++)
		{
			if(i==0)
			{
				buffer.append(this.getBook(books[i],true));
			}
			else
				buffer.append(" and "+this.getBook(books[i],false));
		}
		
		return buffer;
	}
	
	public StringBuffer getBook(String books,boolean flag)
	{
		StringBuffer buffer=new StringBuffer();
		if(books.indexOf("^")!=-1)
		{
			String[] bookInfo=books.split("\\^",2);
			
			String bookId=bookInfo[0];
			
			buffer.append("(");
			buffer.append("book_id = "+bookId);
			buffer.append(this.getBook(bookInfo[1],flag));
			buffer.append(")");
			
			return buffer;
		}
			
		String chaptersInfo=books;
		if(chaptersInfo.indexOf("-")!=-1)
		{
			buffer.append(this.getChapters(chaptersInfo));
		}
		else
		{
			boolean samechapter;
			if(chaptersInfo.indexOf(':')!=-1)
			{
				samechapter=false;
			}
			else
				samechapter=true;
			
			buffer.append(this.getChapterAndPart(chaptersInfo,flag,samechapter));
		}
		
		return buffer;
	}
	
	public StringBuffer getChapters(String chaptersInfo)
	{
		StringBuffer buffer=new StringBuffer();
		boolean samechapter;
		String[] chapters=chaptersInfo.split("-");
		
		for(int j=0;j<chapters.length;j++)
		{
			if(chapters[j].indexOf(':')!=-1)
			{
				samechapter=false;
			}
			else
				samechapter=true;
			
			if(j==0)
			{
				buffer.append(this.getChapterAndPart(chapters[j],true,samechapter));
			}
			else
			{
				buffer.append(this.getChapterAndPart(chapters[j],false,samechapter));
			}
		}

		return buffer;
	}
	
	public StringBuffer getChapterAndPart(String chapters,boolean flag,boolean samechapter)
	{
		StringBuffer buffer=new StringBuffer();
		
		String chapter,part;
		if(chapters.indexOf(":")!=-1)
		{
			String[] chapterAndPart=chapters.split(":");
			
			chapter=chapterAndPart[0];
			part=chapterAndPart[1];
			
			if(flag)
			{
				if(samechapter)
				{
					buffer.append(" and chapter_id = "+chapter);
				}
				else
					buffer.append(" and chapter_id >= "+chapter);
				
				buffer.append(" and part_id >= "+part);
			}
			else
			{
				if(samechapter)
				{
					buffer.append(" and chapter_id = "+chapter);
				}
				else
					buffer.append(" and chapter_id <= "+chapter);
				
				buffer.append(" and part_id <= "+part);
			}
		}
		else
		{
			System.err.println(chapters);
			part=chapters;
			
			if(flag)
			{
				buffer.append(" and part_id >= "+part);
			}
			else
			{
				buffer.append(" and part_id <= "+part);
			}

		}
		
		return buffer;
	}
	
	public String parse(String task)
	{
		this.task=task;
		
		if(this.task.indexOf('~')!=-1)
		{
			return this.getBooks(this.task).toString();
		}
		else
			return this.getBook(this.task,true).toString();
		
	}
}
