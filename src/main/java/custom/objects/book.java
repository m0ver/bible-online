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

public class book extends AbstractData {
	private int bookId;
	private String bookName;
	private String language;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setBookId(int bookId)
	{
		this.bookId=this.setFieldAsInt("bookId",bookId);
	}

	public int getBookId()
	{
		return this.bookId;
	}

	public void setBookName(String bookName)
	{
		this.bookName=this.setFieldAsString("bookName",bookName);
	}

	public String getBookName()
	{
		return this.bookName;
	}

	public void setLanguage(String language)
	{
		this.language=this.setFieldAsString("language",language);
	}

	public String getLanguage()
	{
		return this.language;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("book_id")!=null)	this.setBookId(row.getFieldInfo("book_id").intValue());
		if(row.getFieldInfo("book_name")!=null)	this.setBookName(row.getFieldInfo("book_name").stringValue());
		if(row.getFieldInfo("language")!=null)	this.setLanguage(row.getFieldInfo("language").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"bookId\":"+this.getBookId());
		buffer.append(",\"bookName\":\""+this.getBookName()+"\"");
		buffer.append(",\"language\":\""+this.getLanguage()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}