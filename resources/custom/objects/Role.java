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