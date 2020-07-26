package custom.objects;
import java.io.Serializable;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class JPV extends AbstractData implements Serializable {
	/**
   * Auto Generated Serial Version UID
   */
  private static final long serialVersionUID = 2427198206122054672L;
	private int bookId;
	private int chapterId;
	private int partId;
	private String content;

	public Integer getId()
	{
		return Integer.parseInt(this.Id.toString());
	}

	public void setBookId(int bookId)
	{
		this.bookId=this.setFieldAsInt("bookId",bookId);
	}

	public int getBookId()
	{
		return this.bookId;
	}

	public void setChapterId(int chapterId)
	{
		this.chapterId=this.setFieldAsInt("chapterId",chapterId);
	}

	public int getChapterId()
	{
		return this.chapterId;
	}

	public void setPartId(int partId)
	{
		this.partId=this.setFieldAsInt("partId",partId);
	}

	public int getPartId()
	{
		return this.partId;
	}

	public void setContent(String content)
	{
		this.content=this.setFieldAsString("content",content);
	}

	public String getContent()
	{
		return this.content;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").intValue());
		if(row.getFieldInfo("book_id")!=null)	this.setBookId(row.getFieldInfo("book_id").intValue());
		if(row.getFieldInfo("chapter_id")!=null)	this.setChapterId(row.getFieldInfo("chapter_id").intValue());
		if(row.getFieldInfo("part_id")!=null)	this.setPartId(row.getFieldInfo("part_id").intValue());
		if(row.getFieldInfo("content")!=null)	this.setContent(row.getFieldInfo("content").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":"+this.getId());
		buffer.append(",\"bookId\":"+this.getBookId());
		buffer.append(",\"chapterId\":"+this.getChapterId());
		buffer.append(",\"partId\":"+this.getPartId());
		buffer.append(",\"content\":\""+this.getContent()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}