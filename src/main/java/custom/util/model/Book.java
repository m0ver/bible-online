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
package custom.util.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Book implements Node {
	private List<Chapter> chapters;
	private int bookId;
	private Node fromChapter;
	private Node toChapter;

	public Book(int bookId) {
		this.bookId = bookId;
		this.chapters = new ArrayList<Chapter>();
	}

	public void newChapter(Chapter chapter) {
		this.chapters.add(chapter);
	}

	public void fromNode(Node node) {
		// TODO Auto-generated method stub
		this.fromChapter = node;
	}

	public void toNode(Node node) {
		// TODO Auto-generated method stub
		this.toChapter = node;
	}

	public String toSQL() {
		if (this.chapters.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			Iterator<Chapter> iterator = this.chapters.iterator();
			while (iterator.hasNext()) {

				if (buffer.length() == 0)
					buffer.append(iterator.next().toSQL());
				else
					buffer.append(" or ").append(iterator.next().toSQL());
			}
			return " (book_id=" + bookId + " and (" + buffer.toString() + "))";
		} else if (this.fromChapter.getId() < this.toChapter.getId()) {

			return " (book_id="
					+ bookId
					+ " and (chapter_id>="
					+ this.fromChapter.getId()
					+ (this.fromChapter.firstNode() != null ? " and part_id>="
							+ this.fromChapter.firstNode().getId() : "")
					+ ") and (chapter_id<="
					+ this.toChapter.getId()
					+ (this.toChapter.firstNode() != null ? " and part_id<="
							+ this.toChapter.firstNode().getId() : "") + ")) ";
		}

		return " book_id=" + bookId;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return this.bookId;
	}

	public Node firstNode() {
		// TODO Auto-generated method stub
		if (this.chapters.size() > 0)
			return this.chapters.get(0);

		return null;
	}
}
