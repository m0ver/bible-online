package custom.objects;

import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class plan extends AbstractData {
	private Date date;
	private String task;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setDate(Date date)
	{
		this.date=this.setFieldAsDate("date",date);
	}

	public Date getDate()
	{
		return this.date;
	}

	public void setTask(String task)
	{
		this.task=this.setFieldAsString("task",task);
	}

	public String getTask()
	{
		return this.task;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateValue());
		if(row.getFieldInfo("task")!=null)	this.setTask(row.getFieldInfo("task").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append(",\"task\":\""+this.getTask()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}