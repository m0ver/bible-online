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

public class Member extends AbstractData {
	private String userId;
	private String groupId;

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

	public void setGroupId(String groupId)
	{
		this.groupId=this.setFieldAsString("groupId",groupId);
	}

	public String getGroupId()
	{
		return this.groupId;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("group_id")!=null)	this.setGroupId(row.getFieldInfo("group_id").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"groupId\":\""+this.getGroupId()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}