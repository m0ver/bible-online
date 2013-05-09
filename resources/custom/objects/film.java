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

public class film extends AbstractData {
	private String userId;
	private String videoName;
	private String videoDescription;
	private String videoScreenshots;
	private int hits;
	private int sort;
	private int status;
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

	public void setVideoName(String videoName)
	{
		this.videoName=this.setFieldAsString("videoName",videoName);
	}

	public String getVideoName()
	{
		return this.videoName;
	}

	public void setVideoDescription(String videoDescription)
	{
		this.videoDescription=this.setFieldAsString("videoDescription",videoDescription);
	}

	public String getVideoDescription()
	{
		return this.videoDescription;
	}

	public void setVideoScreenshots(String videoScreenshots)
	{
		this.videoScreenshots=this.setFieldAsString("videoScreenshots",videoScreenshots);
	}

	public String getVideoScreenshots()
	{
		return this.videoScreenshots;
	}

	public void setHits(int hits)
	{
		this.hits=this.setFieldAsInt("hits",hits);
	}

	public int getHits()
	{
		return this.hits;
	}

	public void setSort(int sort)
	{
		this.sort=this.setFieldAsInt("sort",sort);
	}

	public int getSort()
	{
		return this.sort;
	}

	public void setStatus(int status)
	{
		this.status=this.setFieldAsInt("status",status);
	}

	public int getStatus()
	{
		return this.status;
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
		if(row.getFieldInfo("video_name")!=null)	this.setVideoName(row.getFieldInfo("video_name").stringValue());
		if(row.getFieldInfo("video_description")!=null)	this.setVideoDescription(row.getFieldInfo("video_description").stringValue());
		if(row.getFieldInfo("video_screenshots")!=null)	this.setVideoScreenshots(row.getFieldInfo("video_screenshots").stringValue());
		if(row.getFieldInfo("hits")!=null)	this.setHits(row.getFieldInfo("hits").intValue());
		if(row.getFieldInfo("sort")!=null)	this.setSort(row.getFieldInfo("sort").intValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").intValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"videoName\":\""+this.getVideoName()+"\"");
		buffer.append(",\"videoDescription\":\""+this.getVideoDescription()+"\"");
		buffer.append(",\"videoScreenshots\":\""+this.getVideoScreenshots()+"\"");
		buffer.append(",\"hits\":"+this.getHits());
		buffer.append(",\"sort\":"+this.getSort());
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}