/*******************************************************************************
 * Copyright  (c) 2013, 2025 Mover Zhou
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

public class bible_section extends AbstractData {
    private String version = "ALL";
    private String language = "zh_CN";
    private int bookId;
    private int chapterId;
    private int partId;
    private String title = "";
    private String parallelRef = "";
    private int isParagraph = 1;

    public String getId() {
        return String.valueOf(this.Id);
    }

    public void setVersion(String version) {
        this.version = this.setFieldAsString("version", version);
    }

    public String getVersion() {
        return this.version;
    }

    public void setLanguage(String language) {
        this.language = this.setFieldAsString("language", language);
    }

    public String getLanguage() {
        return this.language;
    }

    public void setBookId(int bookId) {
        this.bookId = this.setFieldAsInt("bookId", bookId);
    }

    public int getBookId() {
        return this.bookId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = this.setFieldAsInt("chapterId", chapterId);
    }

    public int getChapterId() {
        return this.chapterId;
    }

    public void setPartId(int partId) {
        this.partId = this.setFieldAsInt("partId", partId);
    }

    public int getPartId() {
        return this.partId;
    }

    public void setTitle(String title) {
        this.title = this.setFieldAsString("title", title);
    }

    public String getTitle() {
        return this.title;
    }

    public void setParallelRef(String parallelRef) {
        this.parallelRef = this.setFieldAsString("parallelRef", parallelRef);
    }

    public String getParallelRef() {
        return this.parallelRef;
    }

    public void setIsParagraph(int isParagraph) {
        this.isParagraph = this.setFieldAsInt("isParagraph", isParagraph);
    }

    public int getIsParagraph() {
        return this.isParagraph;
    }

    @Override
    public void setData(Row row) {
        if (row.getFieldInfo("id") != null) this.setId(row.getFieldInfo("id").stringValue());
        if (row.getFieldInfo("version") != null) this.setVersion(row.getFieldInfo("version").stringValue());
        if (row.getFieldInfo("language") != null) this.setLanguage(row.getFieldInfo("language").stringValue());
        if (row.getFieldInfo("book_id") != null) this.setBookId(row.getFieldInfo("book_id").intValue());
        if (row.getFieldInfo("chapter_id") != null) this.setChapterId(row.getFieldInfo("chapter_id").intValue());
        if (row.getFieldInfo("part_id") != null) this.setPartId(row.getFieldInfo("part_id").intValue());
        if (row.getFieldInfo("title") != null) this.setTitle(row.getFieldInfo("title").stringValue());
        if (row.getFieldInfo("parallel_ref") != null) this.setParallelRef(row.getFieldInfo("parallel_ref").stringValue());
        if (row.getFieldInfo("is_paragraph") != null) this.setIsParagraph(row.getFieldInfo("is_paragraph").intValue());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"Id\":\"").append(this.getId()).append("\"");
        buffer.append(",\"version\":\"").append(this.getVersion()).append("\"");
        buffer.append(",\"language\":\"").append(this.getLanguage()).append("\"");
        buffer.append(",\"bookId\":").append(this.getBookId());
        buffer.append(",\"chapterId\":").append(this.getChapterId());
        buffer.append(",\"partId\":").append(this.getPartId());
        buffer.append(",\"title\":\"").append(this.getTitle()).append("\"");
        buffer.append(",\"parallelRef\":\"").append(this.getParallelRef()).append("\"");
        buffer.append(",\"isParagraph\":").append(this.getIsParagraph());
        buffer.append("}");
        return buffer.toString();
    }
}
