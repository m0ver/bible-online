package custom.objects;

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class keyword extends AbstractData {
	private String keyword;
	private int visit;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setKeyword(String keyword)
	{
		this.keyword=this.setFieldAsString("keyword",keyword);
	}

	public String getKeyword()
	{
		return this.keyword;
	}

	public void setVisit(int visit)
	{
		this.visit=this.setFieldAsInt("visit",visit);
	}

	public int getVisit()
	{
		return this.visit;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("keyword")!=null)	this.setKeyword(row.getFieldInfo("keyword").stringValue());
		if(row.getFieldInfo("visit")!=null)	this.setVisit(row.getFieldInfo("visit").intValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"keyword\":\""+this.getKeyword()+"\"");
		buffer.append(",\"visit\":"+this.getVisit());
		buffer.append("}");
		return buffer.toString();
	}

}