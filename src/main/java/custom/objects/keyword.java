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