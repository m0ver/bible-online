package custom.objects;

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class subscription extends AbstractData {
	private String email;
	private String list;
	private boolean available;

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

	public void setList(String list)
	{
		this.list=this.setFieldAsString("list",list);
	}

	public String getList()
	{
		return this.list;
	}

	public void setAvailable(boolean available)
	{
		this.available=this.setFieldAsBoolean("available",available);
	}

	public boolean getAvailable()
	{
		return this.available;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("email")!=null)	this.setEmail(row.getFieldInfo("email").stringValue());
		if(row.getFieldInfo("list")!=null)	this.setList(row.getFieldInfo("list").stringValue());
		if(row.getFieldInfo("available")!=null)	this.setAvailable(row.getFieldInfo("available").booleanValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"email\":\""+this.getEmail()+"\"");
		buffer.append(",\"list\":\""+this.getList()+"\"");
		buffer.append(",\"available\":"+this.getAvailable());
		buffer.append("}");
		return buffer.toString();
	}

}