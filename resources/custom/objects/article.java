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

public class article extends AbstractData {
	private String userId;
	private String author;
	private int type;
	private String topicId;
	private String title;
	private String content;
	private Date date;
	private Date updateTime;
	private boolean status;

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

	public void setAuthor(String author)
	{
		this.author=this.setFieldAsString("author",author);
	}

	public String getAuthor()
	{
		return this.author;
	}

	public void setType(int type)
	{
		this.type=this.setFieldAsInt("type",type);
	}

	public int getType()
	{
		return this.type;
	}

	public void setTopicId(String topicId)
	{
		this.topicId=this.setFieldAsString("topicId",topicId);
	}

	public String getTopicId()
	{
		return this.topicId;
	}

	public void setTitle(String title)
	{
		this.title=this.setFieldAsString("title",title);
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setContent(String content)
	{
		this.content=this.setFieldAsString("content",content);
	}

	public String getContent()
	{
		return this.content;
	}

	public void setDate(Date date)
	{
		this.date=this.setFieldAsDate("date",date);
	}

	public Date getDate()
	{
		return this.date;
	}

	public void setUpdateTime(Date updateTime)
	{
		this.updateTime=this.setFieldAsDate("updateTime",updateTime);
	}

	public Date getUpdateTime()
	{
		return this.updateTime;
	}

	public void setStatus(boolean status)
	{
		this.status=this.setFieldAsBoolean("status",status);
	}

	public boolean getStatus()
	{
		return this.status;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("author")!=null)	this.setAuthor(row.getFieldInfo("author").stringValue());
		if(row.getFieldInfo("type")!=null)	this.setType(row.getFieldInfo("type").intValue());
		if(row.getFieldInfo("topic_id")!=null)	this.setTopicId(row.getFieldInfo("topic_id").stringValue());
		if(row.getFieldInfo("title")!=null)	this.setTitle(row.getFieldInfo("title").stringValue());
		if(row.getFieldInfo("content")!=null)	this.setContent(row.getFieldInfo("content").stringValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateValue());
		if(row.getFieldInfo("update_time")!=null)	this.setUpdateTime(row.getFieldInfo("update_time").dateValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").booleanValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"author\":\""+this.getAuthor()+"\"");
		buffer.append(",\"type\":"+this.getType());
		buffer.append(",\"topicId\":\""+this.getTopicId()+"\"");
		buffer.append(",\"title\":\""+this.getTitle()+"\"");
		buffer.append(",\"content\":\""+this.getContent()+"\"");
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append(",\"updateTime\":\""+this.getUpdateTime()+"\"");
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append("}");
		return buffer.toString();
	}

}