package custom.objects;

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class Group extends AbstractData {
	private String roles;
	private String name;
	private String description;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setRoles(String roles)
	{
		this.roles=this.setFieldAsString("roles",roles);
	}

	public String getRoles()
	{
		return this.roles;
	}

	public void setName(String name)
	{
		this.name=this.setFieldAsString("name",name);
	}

	public String getName()
	{
		return this.name;
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
		if(row.getFieldInfo("roles")!=null)	this.setRoles(row.getFieldInfo("roles").stringValue());
		if(row.getFieldInfo("name")!=null)	this.setName(row.getFieldInfo("name").stringValue());
		if(row.getFieldInfo("description")!=null)	this.setDescription(row.getFieldInfo("description").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"roles\":\""+this.getRoles()+"\"");
		buffer.append(",\"name\":\""+this.getName()+"\"");
		buffer.append(",\"description\":\""+this.getDescription()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}