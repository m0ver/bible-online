package custom.objects;

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class Role extends AbstractData {
	private String role;
	private String rights;
	private String description;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setRole(String role)
	{
		this.role=this.setFieldAsString("role",role);
	}

	public String getRole()
	{
		return this.role;
	}

	public void setRights(String rights)
	{
		this.rights=this.setFieldAsString("rights",rights);
	}

	public String getRights()
	{
		return this.rights;
	}

	public void setDescription(String description)
	{
		this.description=this.setFieldAsString("description",description);
	}

	public String getDescription()
	{
		return this.description;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("role")!=null)	this.setRole(row.getFieldInfo("role").stringValue());
		if(row.getFieldInfo("rights")!=null)	this.setRights(row.getFieldInfo("rights").stringValue());
		if(row.getFieldInfo("description")!=null)	this.setDescription(row.getFieldInfo("description").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"role\":\""+this.getRole()+"\"");
		buffer.append(",\"rights\":\""+this.getRights()+"\"");
		buffer.append(",\"description\":\""+this.getDescription()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}