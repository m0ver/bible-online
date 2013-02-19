package custom.util.model;

public class Section implements Node {

	private int sectionId;

	public Section(int sectionId) {
		this.sectionId = sectionId;
	}

	public String toSQL() {
		// TODO Auto-generated method stub
		return "part_id=" + this.sectionId;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return this.sectionId;
	}

	public Node firstNode() {
		// TODO Auto-generated method stub
		return null;
	}

}
