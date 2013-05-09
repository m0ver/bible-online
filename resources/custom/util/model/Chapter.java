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

public class Chapter implements Node {
	private List<Section> sections;
	private int chapterId;
	private int from_partId;
	private int to_partId;

	public Chapter(int chapterId) {
		this.chapterId = chapterId;
		this.from_partId = this.to_partId = 0;
		this.sections = new ArrayList<Section>();
	}

	public void newSection(Section section) {
		this.sections.add(section);
	}

	public void fromNode(Node node) {
		// TODO Auto-generated method stub
		this.from_partId = node.getId();
	}

	public void toNode(Node node) {
		// TODO Auto-generated method stub
		this.to_partId = node.getId();
	}

	public String toSQL() {

		if (this.sections.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			Iterator<Section> iterator = this.sections.iterator();
			while (iterator.hasNext()) {
				buffer.append(" and ").append(iterator.next().toSQL());
			}
			return "(chapter_id=" + chapterId + buffer.toString() + ")";
		} else if (this.from_partId != this.to_partId) {
			return "(chapter_id=" + chapterId + " and part_id>="
					+ this.from_partId + " and part_id<="
					+ this.to_partId + ")";
		}

		return " chapter_id=" + chapterId;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return this.chapterId;
	}

	public Node firstNode() {
		// TODO Auto-generated method stub
		if(this.sections.size()>0)
		return this.sections.get(0);
		
		return null;
	}

}
