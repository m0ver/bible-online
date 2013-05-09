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