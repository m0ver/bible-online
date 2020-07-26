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