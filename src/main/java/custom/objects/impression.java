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
package custom.objects;

import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class impression extends AbstractData {
	private String userId;
	private int type;
	private String content;
	private int bookId;
	private int chapterId;
	private Date date;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setUserId(String userId)
	{
		this.userId=this.setFieldAsString("userId",userId);
	}

	public String getUserId()
	{
		return this.userId;
	}

	public void setType(int type)
	{
		this.type=this.setFieldAsInt("type",type);
	}

	public int getType()
	{
		return this.type;
	}

	public void setContent(String content)
	{
		this.content=this.setFieldAsString("content",content);
	}

	public String getContent()
	{
		return this.content;
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

	public void setDate(Date date)
	{
		this.date=this.setFieldAsDate("date",date);
	}

	public Date getDate()
	{
		return this.date;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("type")!=null)	this.setType(row.getFieldInfo("type").intValue());
		if(row.getFieldInfo("content")!=null)	this.setContent(row.getFieldInfo("content").stringValue());
		if(row.getFieldInfo("book_id")!=null)	this.setBookId(row.getFieldInfo("book_id").intValue());
		if(row.getFieldInfo("chapter_id")!=null)	this.setChapterId(row.getFieldInfo("chapter_id").intValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"type\":"+this.getType());
		buffer.append(",\"content\":\""+this.getContent()+"\"");
		buffer.append(",\"bookId\":"+this.getBookId());
		buffer.append(",\"chapterId\":"+this.getChapterId());
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}