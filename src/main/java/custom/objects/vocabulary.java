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

import java.time.LocalDateTime;
import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class vocabulary extends AbstractData {
	private String userId;
	private String word;
	private String phoneticSymbol;
	private String interpretation;
	private String comment;
	private LocalDateTime date;
	private String referenceLink;

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

	public void setWord(String word)
	{
		this.word=this.setFieldAsString("word",word);
	}

	public String getWord()
	{
		return this.word;
	}

	public void setPhoneticSymbol(String phoneticSymbol)
	{
		this.phoneticSymbol=this.setFieldAsString("phoneticSymbol",phoneticSymbol);
	}

	public String getPhoneticSymbol()
	{
		return this.phoneticSymbol;
	}

	public void setInterpretation(String interpretation)
	{
		this.interpretation=this.setFieldAsString("interpretation",interpretation);
	}

	public String getInterpretation()
	{
		return this.interpretation;
	}

	public void setComment(String comment)
	{
		this.comment=this.setFieldAsString("comment",comment);
	}

	public String getComment()
	{
		return this.comment;
	}

	public void setDate(LocalDateTime date)
	{
		this.date=this.setFieldAsDateTime("date",date);
	}

	public LocalDateTime getDate()
	{
		return this.date;
	}

	public void setReferenceLink(String referenceLink)
	{
		this.referenceLink=this.setFieldAsString("referenceLink",referenceLink);
	}

	public String getReferenceLink()
	{
		return this.referenceLink;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("user_id")!=null)	this.setUserId(row.getFieldInfo("user_id").stringValue());
		if(row.getFieldInfo("word")!=null)	this.setWord(row.getFieldInfo("word").stringValue());
		if(row.getFieldInfo("phonetic_symbol")!=null)	this.setPhoneticSymbol(row.getFieldInfo("phonetic_symbol").stringValue());
		if(row.getFieldInfo("interpretation")!=null)	this.setInterpretation(row.getFieldInfo("interpretation").stringValue());
		if(row.getFieldInfo("comment")!=null)	this.setComment(row.getFieldInfo("comment").stringValue());
		if(row.getFieldInfo("date")!=null)	this.setDate(row.getFieldInfo("date").dateTimeValue());
		if(row.getFieldInfo("reference_link")!=null)	this.setReferenceLink(row.getFieldInfo("reference_link").stringValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"userId\":\""+this.getUserId()+"\"");
		buffer.append(",\"word\":\""+this.getWord()+"\"");
		buffer.append(",\"phoneticSymbol\":\""+this.getPhoneticSymbol()+"\"");
		buffer.append(",\"interpretation\":\""+this.getInterpretation()+"\"");
		buffer.append(",\"comment\":\""+this.getComment()+"\"");
		buffer.append(",\"date\":\""+this.getDate()+"\"");
		buffer.append(",\"referenceLink\":\""+this.getReferenceLink()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}