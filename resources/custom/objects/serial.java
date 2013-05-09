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

public class serial extends AbstractData {
	private String number;
	private String userId;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setNumber(String number)
	{
		this.number=this.setFieldAsString("number",number);
	}

	public String getNumber()
	{
		return this.number;
	}

	public void setUserId(String userId)
	{
		this.userId=this.setFieldAsString("userId",userId);
	}

	public String getUserId()
	{
		return this.userId;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("number")!=null)	this.setNumber(row.getFieldInfo("number").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"number\":\""+this.getNumber()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}