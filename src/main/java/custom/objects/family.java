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

import org.tinystruct.data.component.AbstractData;
import org.tinystruct.data.component.Row;

public class family extends AbstractData {
	private String userId;
	private String name;
	private String description;
	private int status;
	private int members;

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

	public void setStatus(int status)
	{
		this.status=this.setFieldAsInt("status",status);
	}

	public int getStatus()
	{
		return this.status;
	}

	public void setMembers(int members)
	{
		this.members=this.setFieldAsInt("members",members);
	}

	public int getMembers()
	{
		return this.members;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("name")!=null)	this.setName(row.getFieldInfo("name").stringValue());
		if(row.getFieldInfo("description")!=null)	this.setDescription(row.getFieldInfo("description").stringValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").intValue());
		if(row.getFieldInfo("members")!=null)	this.setMembers(row.getFieldInfo("members").intValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"name\":\""+this.getName()+"\"");
		buffer.append(",\"description\":\""+this.getDescription()+"\"");
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append(",\"members\":"+this.getMembers());
		buffer.append("}");
		return buffer.toString();
	}

}