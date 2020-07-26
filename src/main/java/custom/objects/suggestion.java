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

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class suggestion extends AbstractData {
	private String email;
	private String title;
	private String content;
	private Date postDate;
	private String IP;
	private boolean status;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setEmail(String email)
	{
		this.email=this.setFieldAsString("email",email);
	}

	public String getEmail()
	{
		return this.email;
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

	public void setPostDate(Date postDate)
	{
		this.postDate=this.setFieldAsDate("postDate",postDate);
	}

	public Date getPostDate()
	{
		return this.postDate;
	}

	public void setIP(String IP)
	{
		this.IP=this.setFieldAsString("IP",IP);
	}

	public String getIP()
	{
		return this.IP;
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
		if(row.getFieldInfo("email")!=null)	this.setEmail(row.getFieldInfo("email").stringValue());
		if(row.getFieldInfo("title")!=null)	this.setTitle(row.getFieldInfo("title").stringValue());
		if(row.getFieldInfo("content")!=null)	this.setContent(row.getFieldInfo("content").stringValue());
		if(row.getFieldInfo("post_date")!=null)	this.setPostDate(row.getFieldInfo("post_date").dateValue());
		if(row.getFieldInfo("IP")!=null)	this.setIP(row.getFieldInfo("IP").stringValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").booleanValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"email\":\""+this.getEmail()+"\"");
		buffer.append(",\"title\":\""+this.getTitle()+"\"");
		buffer.append(",\"content\":\""+this.getContent()+"\"");
		buffer.append(",\"postDate\":\""+this.getPostDate()+"\"");
		buffer.append(",\"IP\":\""+this.getIP()+"\"");
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append("}");
		return buffer.toString();
	}

}