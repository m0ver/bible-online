package custom.objects;

import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class conversation extends AbstractData {
	private String userId;
	private String fromUserId;
	private int groupId;
	private String content;
	private Date postDate;
	private int checked;
	private int read;

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

	public void setFromUserId(String fromUserId)
	{
		this.fromUserId=this.setFieldAsString("fromUserId",fromUserId);
	}

	public String getFromUserId()
	{
		return this.fromUserId;
	}

	public void setGroupId(int groupId)
	{
		this.groupId=this.setFieldAsInt("groupId",groupId);
	}

	public int getGroupId()
	{
		return this.groupId;
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

	public void setChecked(int checked)
	{
		this.checked=this.setFieldAsInt("checked",checked);
	}

	public int getChecked()
	{
		return this.checked;
	}

	public void setRead(int read)
	{
		this.read=this.setFieldAsInt("read",read);
	}

	public int getRead()
	{
		return this.read;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("from_user_id")!=null)	this.setFromUserId(row.getFieldInfo("from_user_id").stringValue());
		if(row.getFieldInfo("group_id")!=null)	this.setGroupId(row.getFieldInfo("group_id").intValue());
		if(row.getFieldInfo("content")!=null)	this.setContent(row.getFieldInfo("content").stringValue());
		if(row.getFieldInfo("post_date")!=null)	this.setPostDate(row.getFieldInfo("post_date").dateValue());
		if(row.getFieldInfo("checked")!=null)	this.setChecked(row.getFieldInfo("checked").intValue());
		if(row.getFieldInfo("read")!=null)	this.setRead(row.getFieldInfo("read").intValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"fromUserId\":\""+this.getFromUserId()+"\"");
		buffer.append(",\"groupId\":"+this.getGroupId());
		buffer.append(",\"content\":\""+this.getContent()+"\"");
		buffer.append(",\"postDate\":\""+this.getPostDate()+"\"");
		buffer.append(",\"checked\":"+this.getChecked());
		buffer.append(",\"read\":"+this.getRead());
		buffer.append("}");
		return buffer.toString();
	}

}