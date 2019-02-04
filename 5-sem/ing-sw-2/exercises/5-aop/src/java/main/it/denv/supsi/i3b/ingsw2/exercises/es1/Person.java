package it.denv.supsi.i3b.ingsw2.exercises.es1;

public class Person extends Component implements IAcceptor {
	private String name;
	private String surname;

	public Person(String name, String surname){
		this.name = name;
		this.surname = surname;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}
