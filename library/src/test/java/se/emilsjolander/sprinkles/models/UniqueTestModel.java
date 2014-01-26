package se.emilsjolander.sprinkles.models;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.annotations.Unique;

@Table("UniqueTests")
public class UniqueTestModel extends AbsTestModel {

	@Unique
    @Column("name")
	private String name;

	public String getName() {
		return name;
	}

	public UniqueTestModel setName(String name) {
		this.name = name;
		return this;
	}
}
