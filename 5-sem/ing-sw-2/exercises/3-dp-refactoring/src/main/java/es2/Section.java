package es2;

public class Section extends Component {
	private String name;

	public Section(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}