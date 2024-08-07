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

public class Log extends AbstractData {
	private String userId;
	private int actionType;
	private String action;
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

	public void setActionType(int actionType)
	{
		this.actionType=this.setFieldAsInt("actionType",actionType);
	}

	public int getActionType()
	{
		return this.actionType;
	}

	public void setAction(String action)
	{
		this.action=this.setFieldAsString("action",action);
	}

	public String getAction()
	{
		return this.action;
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
		if(row.getFieldInfo("action_type")!=null)	this.setActionType(row.getFieldInfo("action_type").intValue());
		if(row.getFieldInfo("action")!=null)	this.setAction(row.getFieldInfo("action").stringValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"actionType\":"+this.getActionType());
		buffer.append(",\"action\":\""+this.getAction()+"\"");
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}