package custom.objects;

import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class report extends AbstractData {
	private String userId;
	private String bibleId;
	private int languageId;
	private String updatedContent;
	private Date modifiedTime;
	private int status;

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

	public void setBibleId(String bibleId)
	{
		this.bibleId=this.setFieldAsString("bibleId",bibleId);
	}

	public String getBibleId()
	{
		return this.bibleId;
	}

	public void setLanguageId(int languageId)
	{
		this.languageId=this.setFieldAsInt("languageId",languageId);
	}

	public int getLanguageId()
	{
		return this.languageId;
	}

	public void setUpdatedContent(String updatedContent)
	{
		this.updatedContent=this.setFieldAsString("updatedContent",updatedContent);
	}

	public String getUpdatedContent()
	{
		return this.updatedContent;
	}

	public void setModifiedTime(Date modifiedTime)
	{
		this.modifiedTime=this.setFieldAsDate("modifiedTime",modifiedTime);
	}

	public Date getModifiedTime()
	{
		return this.modifiedTime;
	}

	public void setStatus(int status)
	{
		this.status=this.setFieldAsInt("status",status);
	}

	public int getStatus()
	{
		return this.status;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("bible_id")!=null)	this.setBibleId(row.getFieldInfo("bible_id").stringValue());
		if(row.getFieldInfo("language_id")!=null)	this.setLanguageId(row.getFieldInfo("language_id").intValue());
		if(row.getFieldInfo("updated_content")!=null)	this.setUpdatedContent(row.getFieldInfo("updated_content").stringValue());
		if(row.getFieldInfo("modified_time")!=null)	this.setModifiedTime(row.getFieldInfo("modified_time").dateValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").intValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"bibleId\":\""+this.getBibleId()+"\"");
		buffer.append(",\"languageId\":"+this.getLanguageId());
		buffer.append(",\"updatedContent\":\""+this.getUpdatedContent()+"\"");
		buffer.append(",\"modifiedTime\":\""+this.getModifiedTime()+"\"");
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append("}");
		return buffer.toString();
	}

}